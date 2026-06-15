package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.*;
import com.example.permission.mapper.*;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.MaintenanceOrderTableDef.MAINTENANCE_ORDER;
import static com.example.permission.entity.table.MaintenancePhotoTableDef.MAINTENANCE_PHOTO;
import static com.example.permission.entity.table.MaintenanceStatusLogTableDef.MAINTENANCE_STATUS_LOG;
import static com.example.permission.entity.table.RoomTableDef.ROOM;

@Service
public class MaintenanceOrderService {

    @Autowired
    private MaintenanceOrderMapper maintenanceOrderMapper;

    @Autowired
    private MaintenancePhotoMapper maintenancePhotoMapper;

    @Autowired
    private MaintenanceStatusLogMapper maintenanceStatusLogMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomChangeLogService roomChangeLogService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private UserFloorPermissionService userFloorPermissionService;

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();
    private static final Map<Integer, String> TYPE_MAP = new HashMap<>();
    private static final Map<Integer, String> PRIORITY_MAP = new HashMap<>();

    static {
        STATUS_MAP.put(1, "待分配");
        STATUS_MAP.put(2, "处理中");
        STATUS_MAP.put(3, "已完成");
        STATUS_MAP.put(4, "已验收");
        STATUS_MAP.put(5, "已关闭");
        TYPE_MAP.put(1, "设施维修");
        TYPE_MAP.put(2, "定期保养");
        TYPE_MAP.put(3, "深度清洁");
        TYPE_MAP.put(4, "设备更换");
        TYPE_MAP.put(5, "装修改造");
        PRIORITY_MAP.put(1, "紧急");
        PRIORITY_MAP.put(2, "高");
        PRIORITY_MAP.put(3, "中");
        PRIORITY_MAP.put(4, "低");
    }

    public static String getStatusText(Integer status) {
        return STATUS_MAP.getOrDefault(status, "未知");
    }

    public static String getTypeText(Integer type) {
        return TYPE_MAP.getOrDefault(type, "未知");
    }

    public static String getPriorityText(Integer priority) {
        return PRIORITY_MAP.getOrDefault(priority, "未知");
    }

    public static Map<Integer, String> getStatusMap() {
        return STATUS_MAP;
    }

    public static Map<Integer, String> getTypeMap() {
        return TYPE_MAP;
    }

    public static Map<Integer, String> getPriorityMap() {
        return PRIORITY_MAP;
    }

    @Transactional(rollbackFor = Exception.class)
    public MaintenanceOrder create(MaintenanceOrder order, List<String> problemPhotoUrls,
                                    Long operatorId, String operatorName, String operator,
                                    String operatorRole, String terminalIp) {
        Room room = roomMapper.selectOneById(order.getRoomId());
        if (room == null) {
            throw new BusinessException("房间不存在");
        }

        QueryWrapper activeQuery = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.ROOM_ID.eq(order.getRoomId()))
                .and(MAINTENANCE_ORDER.STATUS.in(Arrays.asList(1, 2)))
                .and(MAINTENANCE_ORDER.DELETED.eq(0));
        long activeCount = maintenanceOrderMapper.selectCountByQuery(activeQuery);
        if (activeCount > 0) {
            throw new BusinessException("该房间已有待处理的维护单，不能重复创建");
        }

        order.setRoomNumber(room.getRoomNumber());
        order.setOrderNo(generateOrderNo());
        order.setStatus(1);
        order.setCreateUserId(operatorId);
        order.setCreateUserName(operatorName);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setDeleted(0);
        maintenanceOrderMapper.insert(order);

        if (problemPhotoUrls != null && !problemPhotoUrls.isEmpty()) {
            int sort = 0;
            for (String url : problemPhotoUrls) {
                MaintenancePhoto photo = new MaintenancePhoto();
                photo.setOrderId(order.getId());
                photo.setPhotoType(1);
                photo.setPhotoUrl(url);
                photo.setSortOrder(sort++);
                photo.setCreateTime(LocalDateTime.now());
                maintenancePhotoMapper.insert(photo);
            }
        }

        saveStatusLog(order.getId(), order.getOrderNo(), null, 1, operatorId, operatorName, "创建维护单");

        roomService.updateStatus(order.getRoomId(), 6, operatorId, operatorName, "维护单创建：" + order.getOrderNo());

        roomChangeLogService.logRoomChange(order.getRoomId(), 5, "房间状态",
                String.valueOf(room.getStatus()), "6",
                "维护单创建", order.getOrderNo(), order.getId(),
                operatorId, operatorName, operator, operatorRole, terminalIp);

        return order;
    }

    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "WH" + dateStr;
        QueryWrapper query = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.ORDER_NO.like(prefix + "%"))
                .orderBy(MAINTENANCE_ORDER.ORDER_NO.desc())
                .limit(1);
        MaintenanceOrder last = maintenanceOrderMapper.selectOneByQuery(query);
        int seq = 1;
        if (last != null && last.getOrderNo() != null) {
            try {
                String seqStr = last.getOrderNo().substring(prefix.length());
                seq = Integer.parseInt(seqStr) + 1;
            } catch (Exception ignored) {
            }
        }
        return prefix + String.format("%04d", seq);
    }

    @Transactional(rollbackFor = Exception.class)
    public void assign(Long orderId, Long assignedUserId, Long operatorId, String operatorName) {
        MaintenanceOrder order = maintenanceOrderMapper.selectOneById(orderId);
        if (order == null) {
            throw new BusinessException("维护单不存在");
        }
        if (order.getStatus() != 1 && order.getStatus() != 2) {
            throw new BusinessException("当前状态不允许分配");
        }

        SysUser user = sysUserMapper.selectOneById(assignedUserId);
        if (user == null) {
            throw new BusinessException("维修人员不存在");
        }

        Integer oldStatus = order.getStatus();
        order.setAssignedUserId(assignedUserId);
        order.setAssignedUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        order.setAssignTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        maintenanceOrderMapper.update(order);

        saveStatusLog(orderId, order.getOrderNo(), oldStatus, order.getStatus(),
                operatorId, operatorName, "分配给：" + order.getAssignedUserName());
    }

    @Transactional(rollbackFor = Exception.class)
    public void accept(Long orderId, Long operatorId, String operatorName) {
        MaintenanceOrder order = maintenanceOrderMapper.selectOneById(orderId);
        if (order == null) {
            throw new BusinessException("维护单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("当前状态不允许接单");
        }
        if (order.getAssignedUserId() != null && !order.getAssignedUserId().equals(operatorId)) {
            throw new BusinessException("该维护单已分配给其他人员");
        }

        Integer oldStatus = order.getStatus();
        order.setStatus(2);
        order.setAcceptTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        if (order.getAssignedUserId() == null) {
            SysUser user = sysUserMapper.selectOneById(operatorId);
            order.setAssignedUserId(operatorId);
            order.setAssignedUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        }
        maintenanceOrderMapper.update(order);

        saveStatusLog(orderId, order.getOrderNo(), oldStatus, 2, operatorId, operatorName, "接单开始处理");
    }

    @Transactional(rollbackFor = Exception.class)
    public void addProgressNote(Long orderId, String note, List<String> photoUrls,
                                 Long operatorId, String operatorName) {
        MaintenanceOrder order = maintenanceOrderMapper.selectOneById(orderId);
        if (order == null) {
            throw new BusinessException("维护单不存在");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("当前状态不允许更新进度");
        }

        if (photoUrls != null && !photoUrls.isEmpty()) {
            QueryWrapper sortQuery = QueryWrapper.create()
                    .from(MaintenancePhoto.class)
                    .where(MAINTENANCE_PHOTO.ORDER_ID.eq(orderId))
                    .and(MAINTENANCE_PHOTO.PHOTO_TYPE.eq(1))
                    .orderBy(MAINTENANCE_PHOTO.SORT_ORDER.desc())
                    .limit(1);
            MaintenancePhoto last = maintenancePhotoMapper.selectOneByQuery(sortQuery);
            int sort = last != null && last.getSortOrder() != null ? last.getSortOrder() + 1 : 0;
            for (String url : photoUrls) {
                MaintenancePhoto photo = new MaintenancePhoto();
                photo.setOrderId(orderId);
                photo.setPhotoType(1);
                photo.setPhotoUrl(url);
                photo.setSortOrder(sort++);
                photo.setCreateTime(LocalDateTime.now());
                maintenancePhotoMapper.insert(photo);
            }
        }

        saveStatusLog(orderId, order.getOrderNo(), 2, 2, operatorId, operatorName,
                "更新进度：" + note);
    }

    @Transactional(rollbackFor = Exception.class)
    public void finish(Long orderId, BigDecimal actualHours, String usedParts,
                        BigDecimal maintenanceCost, String maintenanceDescription,
                        List<String> afterPhotoUrls, Long operatorId, String operatorName) {
        MaintenanceOrder order = maintenanceOrderMapper.selectOneById(orderId);
        if (order == null) {
            throw new BusinessException("维护单不存在");
        }
        if (order.getStatus() != 2) {
            throw new BusinessException("当前状态不允许提交完成");
        }

        Integer oldStatus = order.getStatus();
        order.setActualHours(actualHours);
        order.setUsedParts(usedParts);
        order.setMaintenanceCost(maintenanceCost != null ? maintenanceCost : BigDecimal.ZERO);
        order.setMaintenanceDescription(maintenanceDescription);
        order.setFinishTime(LocalDateTime.now());
        order.setStatus(3);
        order.setUpdateTime(LocalDateTime.now());
        maintenanceOrderMapper.update(order);

        if (afterPhotoUrls != null && !afterPhotoUrls.isEmpty()) {
            int sort = 0;
            for (String url : afterPhotoUrls) {
                MaintenancePhoto photo = new MaintenancePhoto();
                photo.setOrderId(orderId);
                photo.setPhotoType(2);
                photo.setPhotoUrl(url);
                photo.setSortOrder(sort++);
                photo.setCreateTime(LocalDateTime.now());
                maintenancePhotoMapper.insert(photo);
            }
        }

        saveStatusLog(orderId, order.getOrderNo(), oldStatus, 3, operatorId, operatorName, "提交完成，等待验收");
    }

    @Transactional(rollbackFor = Exception.class)
    public void inspect(Long orderId, Integer inspectResult, String inspectOpinion,
                         String rectificationRequirement, Long operatorId, String operatorName,
                         String operator, String operatorRole, String terminalIp) {
        MaintenanceOrder order = maintenanceOrderMapper.selectOneById(orderId);
        if (order == null) {
            throw new BusinessException("维护单不存在");
        }
        if (order.getStatus() != 3) {
            throw new BusinessException("当前状态不允许验收");
        }

        Integer oldStatus = order.getStatus();
        order.setInspectorId(operatorId);
        order.setInspectorName(operatorName);
        order.setInspectTime(LocalDateTime.now());
        order.setInspectResult(inspectResult);
        order.setInspectOpinion(inspectOpinion);
        order.setUpdateTime(LocalDateTime.now());

        if (inspectResult == 1) {
            order.setStatus(4);
            maintenanceOrderMapper.update(order);
            saveStatusLog(orderId, order.getOrderNo(), oldStatus, 4, operatorId, operatorName,
                    "验收通过：" + inspectOpinion);

            roomService.updateStatus(order.getRoomId(), 4, operatorId, operatorName,
                    "维护验收通过：" + order.getOrderNo());

            roomChangeLogService.logRoomChange(order.getRoomId(), 5, "房间状态",
                    "6", "4",
                    "维护验收通过", order.getOrderNo(), order.getId(),
                    operatorId, operatorName, operator, operatorRole, terminalIp);

            MaintenanceOrder finalOrder = order;
            new Thread(() -> {
                try {
                    Thread.sleep(100);
                    finalOrder.setStatus(5);
                    finalOrder.setUpdateTime(LocalDateTime.now());
                    maintenanceOrderMapper.update(finalOrder);
                    saveStatusLog(orderId, finalOrder.getOrderNo(), 4, 5, operatorId, operatorName, "自动关闭");
                } catch (InterruptedException ignored) {
                }
            }).start();

        } else {
            order.setStatus(2);
            order.setRectificationRequirement(rectificationRequirement);
            maintenanceOrderMapper.update(order);
            saveStatusLog(orderId, order.getOrderNo(), oldStatus, 2, operatorId, operatorName,
                    "验收不通过，整改要求：" + rectificationRequirement);
        }
    }

    private void saveStatusLog(Long orderId, String orderNo, Integer oldStatus, Integer newStatus,
                                Long operatorId, String operatorName, String remark) {
        MaintenanceStatusLog log = new MaintenanceStatusLog();
        log.setOrderId(orderId);
        log.setOrderNo(orderNo);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setRemark(remark);
        log.setCreateTime(LocalDateTime.now());
        maintenanceStatusLogMapper.insert(log);
    }

    public PageResult<MaintenanceOrder> pageList(Integer pageNum, Integer pageSize,
                                                   String orderNo, String roomNumber,
                                                   List<Integer> statusList, List<Integer> typeList,
                                                   List<Integer> priorityList, Long assignedUserId,
                                                   Long currentUserId, boolean canSeeAll) {
        QueryWrapper query = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.DELETED.eq(0));
        if (StringUtils.hasText(orderNo)) {
            query.and(MAINTENANCE_ORDER.ORDER_NO.like(orderNo));
        }
        if (StringUtils.hasText(roomNumber)) {
            query.and(MAINTENANCE_ORDER.ROOM_NUMBER.like(roomNumber));
        }
        if (statusList != null && !statusList.isEmpty()) {
            query.and(MAINTENANCE_ORDER.STATUS.in(statusList));
        }
        if (typeList != null && !typeList.isEmpty()) {
            query.and(MAINTENANCE_ORDER.MAINTENANCE_TYPE.in(typeList));
        }
        if (priorityList != null && !priorityList.isEmpty()) {
            query.and(MAINTENANCE_ORDER.PRIORITY.in(priorityList));
        }
        if (assignedUserId != null) {
            query.and(MAINTENANCE_ORDER.ASSIGNED_USER_ID.eq(assignedUserId));
        }
        if (!canSeeAll) {
            query.and(MAINTENANCE_ORDER.ASSIGNED_USER_ID.eq(currentUserId));
        }
        query.orderBy(MAINTENANCE_ORDER.PRIORITY.asc(), MAINTENANCE_ORDER.CREATE_TIME.desc());
        Page<MaintenanceOrder> page = maintenanceOrderMapper.paginate(Page.of(pageNum, pageSize), query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public PageResult<MaintenanceOrder> pageListWithPermission(Integer pageNum, Integer pageSize,
                                                                String orderNo, String roomNumber,
                                                                List<Integer> statusList, List<Integer> typeList,
                                                                List<Integer> priorityList, Long assignedUserId,
                                                                Long currentUserId, boolean canSeeAll,
                                                                Long userId) {
        QueryWrapper query = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.DELETED.eq(0));
        if (StringUtils.hasText(orderNo)) {
            query.and(MAINTENANCE_ORDER.ORDER_NO.like(orderNo));
        }
        if (StringUtils.hasText(roomNumber)) {
            query.and(MAINTENANCE_ORDER.ROOM_NUMBER.like(roomNumber));
        }
        if (statusList != null && !statusList.isEmpty()) {
            query.and(MAINTENANCE_ORDER.STATUS.in(statusList));
        }
        if (typeList != null && !typeList.isEmpty()) {
            query.and(MAINTENANCE_ORDER.MAINTENANCE_TYPE.in(typeList));
        }
        if (priorityList != null && !priorityList.isEmpty()) {
            query.and(MAINTENANCE_ORDER.PRIORITY.in(priorityList));
        }
        if (assignedUserId != null) {
            query.and(MAINTENANCE_ORDER.ASSIGNED_USER_ID.eq(assignedUserId));
        }
        if (!canSeeAll) {
            query.and(MAINTENANCE_ORDER.ASSIGNED_USER_ID.eq(currentUserId));
        }

        List<Long> floorIds = userFloorPermissionService.getFloorIdsByUserId(userId);
        if (floorIds != null && !floorIds.isEmpty()) {
            QueryWrapper roomQuery = QueryWrapper.create()
                    .from(Room.class)
                    .select(ROOM.ID)
                    .where(ROOM.FLOOR_ID.in(floorIds));
            List<Long> roomIds = roomMapper.selectListByQuery(roomQuery)
                    .stream()
                    .map(Room::getId)
                    .collect(Collectors.toList());
            if (!roomIds.isEmpty()) {
                query.and(MAINTENANCE_ORDER.ROOM_ID.in(roomIds));
            } else {
                query.and(MAINTENANCE_ORDER.ROOM_ID.eq(-1L));
            }
        }

        query.orderBy(MAINTENANCE_ORDER.PRIORITY.asc(), MAINTENANCE_ORDER.CREATE_TIME.desc());
        Page<MaintenanceOrder> page = maintenanceOrderMapper.paginate(Page.of(pageNum, pageSize), query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public MaintenanceOrder getById(Long id) {
        MaintenanceOrder order = maintenanceOrderMapper.selectOneById(id);
        if (order != null) {
            QueryWrapper problemQuery = QueryWrapper.create()
                    .from(MaintenancePhoto.class)
                    .where(MAINTENANCE_PHOTO.ORDER_ID.eq(id))
                    .and(MAINTENANCE_PHOTO.PHOTO_TYPE.eq(1))
                    .orderBy(MAINTENANCE_PHOTO.SORT_ORDER.asc());
            order.setProblemPhotos(maintenancePhotoMapper.selectListByQuery(problemQuery));

            QueryWrapper afterQuery = QueryWrapper.create()
                    .from(MaintenancePhoto.class)
                    .where(MAINTENANCE_PHOTO.ORDER_ID.eq(id))
                    .and(MAINTENANCE_PHOTO.PHOTO_TYPE.eq(2))
                    .orderBy(MAINTENANCE_PHOTO.SORT_ORDER.asc());
            order.setAfterPhotos(maintenancePhotoMapper.selectListByQuery(afterQuery));

            QueryWrapper logQuery = QueryWrapper.create()
                    .from(MaintenanceStatusLog.class)
                    .where(MAINTENANCE_STATUS_LOG.ORDER_ID.eq(id))
                    .orderBy(MAINTENANCE_STATUS_LOG.CREATE_TIME.asc());
            List<MaintenanceStatusLog> allLogs = maintenanceStatusLogMapper.selectListByQuery(logQuery);
            order.setStatusLogs(allLogs);

            order.setMaintenanceTypeName(getTypeText(order.getMaintenanceType()));
            order.setCreatorName(order.getCreateUserName());

            List<MaintenanceStatusLog> progressList = allLogs.stream()
                    .filter(log -> log.getRemark() != null && log.getRemark().startsWith("更新进度："))
                    .map(log -> {
                        MaintenanceStatusLog p = new MaintenanceStatusLog();
                        p.setId(log.getId());
                        p.setOperatorName(log.getOperatorName());
                        p.setCreateTime(log.getCreateTime());
                        p.setRemark(log.getRemark().substring(6));
                        p.setNote(log.getRemark().substring(6));
                        return p;
                    })
                    .collect(java.util.stream.Collectors.toList());
            order.setProgressList(progressList);
        }
        return order;
    }

    public List<MaintenanceOrder> listByRoomId(Long roomId) {
        QueryWrapper query = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.ROOM_ID.eq(roomId))
                .and(MAINTENANCE_ORDER.DELETED.eq(0))
                .orderBy(MAINTENANCE_ORDER.CREATE_TIME.desc());
        return maintenanceOrderMapper.selectListByQuery(query);
    }

    public Map<String, Object> getRoomMaintenanceStats(Long roomId) {
        Map<String, Object> result = new HashMap<>();
        QueryWrapper query = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.ROOM_ID.eq(roomId))
                .and(MAINTENANCE_ORDER.DELETED.eq(0))
                .orderBy(MAINTENANCE_ORDER.CREATE_TIME.desc());
        List<MaintenanceOrder> orders = maintenanceOrderMapper.selectListByQuery(query);
        result.put("totalCount", orders.size());

        BigDecimal totalCost = BigDecimal.ZERO;
        LocalDateTime lastTime = null;
        for (MaintenanceOrder o : orders) {
            if (o.getMaintenanceCost() != null) {
                totalCost = totalCost.add(o.getMaintenanceCost());
            }
            if (o.getFinishTime() != null && (lastTime == null || o.getFinishTime().isAfter(lastTime))) {
                lastTime = o.getFinishTime();
            }
        }
        result.put("totalCost", totalCost);
        result.put("lastMaintenanceTime", lastTime);

        List<MaintenanceOrder> finished = orders.stream()
                .filter(o -> o.getFinishTime() != null)
                .sorted(Comparator.comparing(MaintenanceOrder::getFinishTime))
                .collect(Collectors.toList());
        if (finished.size() >= 2) {
            long totalDays = 0;
            for (int i = 1; i < finished.size(); i++) {
                totalDays += ChronoUnit.DAYS.between(
                        finished.get(i - 1).getFinishTime().toLocalDate(),
                        finished.get(i).getFinishTime().toLocalDate()
                );
            }
            result.put("avgCycleDays", (double) totalDays / (finished.size() - 1));
        } else {
            result.put("avgCycleDays", null);
        }
        return result;
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> result = new HashMap<>();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = LocalDate.now().plusMonths(1).withDayOfMonth(1).atStartOfDay();

        QueryWrapper pendingQuery = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.STATUS.eq(1))
                .and(MAINTENANCE_ORDER.DELETED.eq(0));
        result.put("pendingCount", maintenanceOrderMapper.selectCountByQuery(pendingQuery));

        QueryWrapper processingQuery = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.STATUS.eq(2))
                .and(MAINTENANCE_ORDER.DELETED.eq(0));
        result.put("processingCount", maintenanceOrderMapper.selectCountByQuery(processingQuery));

        QueryWrapper waitingInspectQuery = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.STATUS.eq(3))
                .and(MAINTENANCE_ORDER.DELETED.eq(0));
        result.put("waitingInspectCount", maintenanceOrderMapper.selectCountByQuery(waitingInspectQuery));

        QueryWrapper monthFinishedQuery = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.STATUS.in(Arrays.asList(4, 5)))
                .and(MAINTENANCE_ORDER.DELETED.eq(0))
                .and(MAINTENANCE_ORDER.FINISH_TIME.ge(monthStart))
                .and(MAINTENANCE_ORDER.FINISH_TIME.lt(monthEnd));
        result.put("monthFinishedCount", maintenanceOrderMapper.selectCountByQuery(monthFinishedQuery));

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long operatorId, String operatorName) {
        MaintenanceOrder order = maintenanceOrderMapper.selectOneById(id);
        if (order == null) {
            throw new BusinessException("维护单不存在");
        }
        order.setDeleted(1);
        order.setUpdateTime(LocalDateTime.now());
        maintenanceOrderMapper.update(order);
        saveStatusLog(id, order.getOrderNo(), order.getStatus(), order.getStatus(),
                operatorId, operatorName, "删除维护单");
    }

    public List<MaintenanceOrder> listAllForExport(String orderNo, String roomNumber,
                                                     List<Integer> statusList, List<Integer> typeList,
                                                     List<Integer> priorityList, Long assignedUserId) {
        QueryWrapper query = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.DELETED.eq(0));
        if (StringUtils.hasText(orderNo)) {
            query.and(MAINTENANCE_ORDER.ORDER_NO.like(orderNo));
        }
        if (StringUtils.hasText(roomNumber)) {
            query.and(MAINTENANCE_ORDER.ROOM_NUMBER.like(roomNumber));
        }
        if (statusList != null && !statusList.isEmpty()) {
            query.and(MAINTENANCE_ORDER.STATUS.in(statusList));
        }
        if (typeList != null && !typeList.isEmpty()) {
            query.and(MAINTENANCE_ORDER.MAINTENANCE_TYPE.in(typeList));
        }
        if (priorityList != null && !priorityList.isEmpty()) {
            query.and(MAINTENANCE_ORDER.PRIORITY.in(priorityList));
        }
        if (assignedUserId != null) {
            query.and(MAINTENANCE_ORDER.ASSIGNED_USER_ID.eq(assignedUserId));
        }
        query.orderBy(MAINTENANCE_ORDER.CREATE_TIME.desc());
        return maintenanceOrderMapper.selectListByQuery(query);
    }
}
