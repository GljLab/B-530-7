package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.common.PageResult;
import com.example.permission.entity.BatchOperation;
import com.example.permission.entity.BatchOperationDetail;
import com.example.permission.entity.MaintenanceOrder;
import com.example.permission.entity.Room;
import com.example.permission.entity.RoomStatusLog;
import com.example.permission.mapper.BatchOperationDetailMapper;
import com.example.permission.mapper.BatchOperationMapper;
import com.example.permission.mapper.MaintenanceOrderMapper;
import com.example.permission.mapper.RoomMapper;
import com.example.permission.mapper.RoomStatusLogMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.example.permission.entity.table.BatchOperationDetailTableDef.BATCH_OPERATION_DETAIL;
import static com.example.permission.entity.table.BatchOperationTableDef.BATCH_OPERATION;
import static com.example.permission.entity.table.MaintenanceOrderTableDef.MAINTENANCE_ORDER;
import static com.example.permission.entity.table.RoomTableDef.ROOM;

@Service
public class BatchOperationService {

    @Autowired
    private BatchOperationMapper batchOperationMapper;

    @Autowired
    private BatchOperationDetailMapper batchOperationDetailMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private RoomStatusLogMapper roomStatusLogMapper;

    @Autowired
    private MaintenanceOrderMapper maintenanceOrderMapper;

    private static final int MAX_BATCH_SIZE = 50;

    private static final int STATUS_OCCUPIED = 3;
    private static final int STATUS_MAINTENANCE = 6;
    private static final int STATUS_IDLE = 1;
    private static final int STATUS_DISABLED = 7;

    private static final int RESULT_SUCCESS = 1;
    private static final int RESULT_FAIL = 2;
    private static final int RESULT_SKIP = 3;

    private static final int OP_TYPE_STATUS = 1;
    private static final int OP_TYPE_ATTR = 2;
    private static final int OP_TYPE_DELETE = 3;

    private static final int BATCH_STATUS_PROCESSING = 1;
    private static final int BATCH_STATUS_FINISHED = 2;

    private String generateBatchNo() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchUpdateStatus(List<Long> roomIds, Integer targetStatus, String reason,
                                                  Long operatorId, String operatorName) {
        if (roomIds == null || roomIds.isEmpty()) {
            throw new BusinessException("请选择要操作的房间");
        }
        if (roomIds.size() > MAX_BATCH_SIZE) {
            throw new BusinessException("单次最多操作" + MAX_BATCH_SIZE + "个房间");
        }
        if (targetStatus == null) {
            throw new BusinessException("请选择目标状态");
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("请填写操作原因");
        }

        String batchNo = generateBatchNo();
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;
        List<Map<String, Object>> details = new ArrayList<>();

        BatchOperation batch = new BatchOperation();
        batch.setBatchNo(batchNo);
        batch.setOperationType(OP_TYPE_STATUS);
        batch.setTargetStatus(targetStatus);
        batch.setOperationReason(reason);
        batch.setOperatorId(operatorId);
        batch.setOperatorName(operatorName);
        batch.setTotalCount(roomIds.size());
        batch.setStatus(BATCH_STATUS_PROCESSING);
        batch.setCreateTime(LocalDateTime.now());
        batchOperationMapper.insert(batch);

        for (Long roomId : roomIds) {
            Room room = roomMapper.selectOneById(roomId);
            Map<String, Object> detail = new HashMap<>();
            detail.put("roomId", roomId);

            if (room == null) {
                detail.put("roomNumber", "");
                detail.put("resultStatus", RESULT_FAIL);
                detail.put("failReason", "房间不存在");
                failCount++;
                saveDetail(batch.getId(), batchNo, 1, roomId, "", RESULT_FAIL, "房间不存在", null, null);
                details.add(detail);
                continue;
            }

            detail.put("roomNumber", room.getRoomNumber());

            if (room.getStatus() != null && (room.getStatus() == STATUS_OCCUPIED || room.getStatus() == STATUS_MAINTENANCE)) {
                detail.put("resultStatus", RESULT_SKIP);
                String skipReason = room.getStatus() == STATUS_OCCUPIED ? "已入住房间不能修改状态" : "维修中房间不能修改状态";
                detail.put("failReason", skipReason);
                skipCount++;
                saveDetail(batch.getId(), batchNo, 1, roomId, room.getRoomNumber(), RESULT_SKIP, skipReason,
                        String.valueOf(room.getStatus()), String.valueOf(room.getStatus()));
                details.add(detail);
                continue;
            }

            try {
                Integer oldStatus = room.getStatus();
                room.setStatus(targetStatus);
                room.setUpdateTime(LocalDateTime.now());
                roomMapper.update(room);

                RoomStatusLog log = new RoomStatusLog();
                log.setRoomId(roomId);
                log.setRoomNumber(room.getRoomNumber());
                log.setOldStatus(oldStatus);
                log.setNewStatus(targetStatus);
                log.setOperatorId(operatorId);
                log.setOperator(operatorName);
                log.setRemark("批量修改状态：" + reason);
                log.setCreateTime(LocalDateTime.now());
                roomStatusLogMapper.insert(log);

                detail.put("resultStatus", RESULT_SUCCESS);
                detail.put("oldStatus", oldStatus);
                detail.put("newStatus", targetStatus);
                successCount++;
                saveDetail(batch.getId(), batchNo, 1, roomId, room.getRoomNumber(), RESULT_SUCCESS, null,
                        String.valueOf(oldStatus), String.valueOf(targetStatus));
            } catch (Exception e) {
                detail.put("resultStatus", RESULT_FAIL);
                detail.put("failReason", e.getMessage());
                failCount++;
                saveDetail(batch.getId(), batchNo, 1, roomId, room.getRoomNumber(), RESULT_FAIL, e.getMessage(),
                        String.valueOf(room.getStatus()), String.valueOf(room.getStatus()));
            }
            details.add(detail);
        }

        batch.setSuccessCount(successCount);
        batch.setFailCount(failCount);
        batch.setSkipCount(skipCount);
        batch.setStatus(BATCH_STATUS_FINISHED);
        batch.setFinishTime(LocalDateTime.now());
        batchOperationMapper.update(batch);

        Map<String, Object> result = new HashMap<>();
        result.put("batchNo", batchNo);
        result.put("totalCount", roomIds.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("skipCount", skipCount);
        result.put("details", details);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchUpdateAttr(List<Long> roomIds, String attrType, Integer attrMode, String attrValue,
                                                String reason, Long operatorId, String operatorName) {
        if (roomIds == null || roomIds.isEmpty()) {
            throw new BusinessException("请选择要操作的房间");
        }
        if (roomIds.size() > MAX_BATCH_SIZE) {
            throw new BusinessException("单次最多操作" + MAX_BATCH_SIZE + "个房间");
        }
        if (!StringUtils.hasText(attrType)) {
            throw new BusinessException("请选择属性类型");
        }
        List<String> validAttrTypes = Arrays.asList("orientation", "viewType", "locationFeatures", "specialTags");
        if (!validAttrTypes.contains(attrType)) {
            throw new BusinessException("属性类型不正确");
        }
        if (attrMode == null || (attrMode != 1 && attrMode != 2 && attrMode != 3)) {
            throw new BusinessException("请选择操作模式");
        }
        if (attrMode != 3 && !StringUtils.hasText(attrValue)) {
            throw new BusinessException("请填写属性值");
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("请填写操作原因");
        }

        String batchNo = generateBatchNo();
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;
        List<Map<String, Object>> details = new ArrayList<>();

        BatchOperation batch = new BatchOperation();
        batch.setBatchNo(batchNo);
        batch.setOperationType(OP_TYPE_ATTR);
        batch.setAttrType(attrType);
        batch.setAttrMode(attrMode);
        batch.setAttrValue(attrValue);
        batch.setOperationReason(reason);
        batch.setOperatorId(operatorId);
        batch.setOperatorName(operatorName);
        batch.setTotalCount(roomIds.size());
        batch.setStatus(BATCH_STATUS_PROCESSING);
        batch.setCreateTime(LocalDateTime.now());
        batchOperationMapper.insert(batch);

        for (Long roomId : roomIds) {
            Room room = roomMapper.selectOneById(roomId);
            Map<String, Object> detail = new HashMap<>();
            detail.put("roomId", roomId);

            if (room == null) {
                detail.put("roomNumber", "");
                detail.put("resultStatus", RESULT_FAIL);
                detail.put("failReason", "房间不存在");
                failCount++;
                saveDetail(batch.getId(), batchNo, 1, roomId, "", RESULT_FAIL, "房间不存在", null, null);
                details.add(detail);
                continue;
            }

            detail.put("roomNumber", room.getRoomNumber());

            try {
                String oldValue = getAttrValue(room, attrType);
                String newValue = calculateNewAttrValue(oldValue, attrMode, attrValue);
                setAttrValue(room, attrType, newValue);
                room.setUpdateTime(LocalDateTime.now());
                roomMapper.update(room);

                RoomStatusLog log = new RoomStatusLog();
                log.setRoomId(roomId);
                log.setRoomNumber(room.getRoomNumber());
                log.setOldStatus(room.getStatus());
                log.setNewStatus(room.getStatus());
                log.setOperatorId(operatorId);
                log.setOperator(operatorName);
                log.setRemark("批量修改属性[" + attrType + "]：" + reason);
                log.setCreateTime(LocalDateTime.now());
                roomStatusLogMapper.insert(log);

                detail.put("resultStatus", RESULT_SUCCESS);
                detail.put("oldValue", oldValue);
                detail.put("newValue", newValue);
                successCount++;
                saveDetail(batch.getId(), batchNo, 1, roomId, room.getRoomNumber(), RESULT_SUCCESS, null, oldValue, newValue);
            } catch (Exception e) {
                detail.put("resultStatus", RESULT_FAIL);
                detail.put("failReason", e.getMessage());
                failCount++;
                saveDetail(batch.getId(), batchNo, 1, roomId, room.getRoomNumber(), RESULT_FAIL, e.getMessage(),
                        getAttrValue(room, attrType), getAttrValue(room, attrType));
            }
            details.add(detail);
        }

        batch.setSuccessCount(successCount);
        batch.setFailCount(failCount);
        batch.setSkipCount(skipCount);
        batch.setStatus(BATCH_STATUS_FINISHED);
        batch.setFinishTime(LocalDateTime.now());
        batchOperationMapper.update(batch);

        Map<String, Object> result = new HashMap<>();
        result.put("batchNo", batchNo);
        result.put("totalCount", roomIds.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("skipCount", skipCount);
        result.put("details", details);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchDeleteRooms(List<Long> roomIds, String reason, Long operatorId, String operatorName) {
        if (roomIds == null || roomIds.isEmpty()) {
            throw new BusinessException("请选择要操作的房间");
        }
        if (roomIds.size() > MAX_BATCH_SIZE) {
            throw new BusinessException("单次最多操作" + MAX_BATCH_SIZE + "个房间");
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("请填写操作原因");
        }

        String batchNo = generateBatchNo();
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;
        List<Map<String, Object>> details = new ArrayList<>();

        BatchOperation batch = new BatchOperation();
        batch.setBatchNo(batchNo);
        batch.setOperationType(OP_TYPE_DELETE);
        batch.setOperationReason(reason);
        batch.setOperatorId(operatorId);
        batch.setOperatorName(operatorName);
        batch.setTotalCount(roomIds.size());
        batch.setStatus(BATCH_STATUS_PROCESSING);
        batch.setCreateTime(LocalDateTime.now());
        batchOperationMapper.insert(batch);

        for (Long roomId : roomIds) {
            Room room = roomMapper.selectOneById(roomId);
            Map<String, Object> detail = new HashMap<>();
            detail.put("roomId", roomId);

            if (room == null) {
                detail.put("roomNumber", "");
                detail.put("resultStatus", RESULT_FAIL);
                detail.put("failReason", "房间不存在");
                failCount++;
                saveDetail(batch.getId(), batchNo, 1, roomId, "", RESULT_FAIL, "房间不存在", null, null);
                details.add(detail);
                continue;
            }

            detail.put("roomNumber", room.getRoomNumber());

            if (room.getStatus() == null || (room.getStatus() != STATUS_IDLE && room.getStatus() != STATUS_DISABLED)) {
                detail.put("resultStatus", RESULT_SKIP);
                detail.put("failReason", "仅空闲或停用状态的房间可删除");
                skipCount++;
                saveDetail(batch.getId(), batchNo, 1, roomId, room.getRoomNumber(), RESULT_SKIP,
                        "仅空闲或停用状态的房间可删除", String.valueOf(room.getStatus()), String.valueOf(room.getStatus()));
                details.add(detail);
                continue;
            }

            QueryWrapper maintenanceQuery = QueryWrapper.create()
                    .from(MaintenanceOrder.class)
                    .where(MAINTENANCE_ORDER.ROOM_ID.eq(roomId))
                    .and(MAINTENANCE_ORDER.DELETED.eq(0))
                    .and(MAINTENANCE_ORDER.STATUS.notIn(Arrays.asList(4, 5)));
            long maintenanceCount = maintenanceOrderMapper.selectCountByQuery(maintenanceQuery);
            if (maintenanceCount > 0) {
                detail.put("resultStatus", RESULT_SKIP);
                detail.put("failReason", "存在未关闭的维护单");
                skipCount++;
                saveDetail(batch.getId(), batchNo, 1, roomId, room.getRoomNumber(), RESULT_SKIP,
                        "存在未关闭的维护单", null, null);
                details.add(detail);
                continue;
            }

            try {
                room.setDeleted(1);
                room.setUpdateTime(LocalDateTime.now());
                roomMapper.update(room);

                RoomStatusLog log = new RoomStatusLog();
                log.setRoomId(roomId);
                log.setRoomNumber(room.getRoomNumber());
                log.setOldStatus(room.getStatus());
                log.setNewStatus(room.getStatus());
                log.setOperatorId(operatorId);
                log.setOperator(operatorName);
                log.setRemark("批量删除房间：" + reason);
                log.setCreateTime(LocalDateTime.now());
                roomStatusLogMapper.insert(log);

                detail.put("resultStatus", RESULT_SUCCESS);
                successCount++;
                saveDetail(batch.getId(), batchNo, 1, roomId, room.getRoomNumber(), RESULT_SUCCESS, null,
                        String.valueOf(room.getStatus()), "已删除");
            } catch (Exception e) {
                detail.put("resultStatus", RESULT_FAIL);
                detail.put("failReason", e.getMessage());
                failCount++;
                saveDetail(batch.getId(), batchNo, 1, roomId, room.getRoomNumber(), RESULT_FAIL, e.getMessage(), null, null);
            }
            details.add(detail);
        }

        batch.setSuccessCount(successCount);
        batch.setFailCount(failCount);
        batch.setSkipCount(skipCount);
        batch.setStatus(BATCH_STATUS_FINISHED);
        batch.setFinishTime(LocalDateTime.now());
        batchOperationMapper.update(batch);

        Map<String, Object> result = new HashMap<>();
        result.put("batchNo", batchNo);
        result.put("totalCount", roomIds.size());
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("skipCount", skipCount);
        result.put("details", details);
        return result;
    }

    public BatchOperation getBatchByNo(String batchNo) {
        QueryWrapper query = QueryWrapper.create()
                .from(BatchOperation.class)
                .where(BATCH_OPERATION.BATCH_NO.eq(batchNo));
        return batchOperationMapper.selectOneByQuery(query);
    }

    public List<BatchOperationDetail> getBatchDetails(String batchNo) {
        QueryWrapper query = QueryWrapper.create()
                .from(BatchOperationDetail.class)
                .where(BATCH_OPERATION_DETAIL.BATCH_NO.eq(batchNo))
                .orderBy(BATCH_OPERATION_DETAIL.CREATE_TIME.asc());
        return batchOperationDetailMapper.selectListByQuery(query);
    }

    public PageResult<BatchOperation> pageList(Integer pageNum, Integer pageSize, String batchNo,
                                                Integer operationType, Long operatorId) {
        QueryWrapper query = QueryWrapper.create()
                .from(BatchOperation.class)
                .where("1=1");
        if (StringUtils.hasText(batchNo)) {
            query.and(BATCH_OPERATION.BATCH_NO.like(batchNo));
        }
        if (operationType != null) {
            query.and(BATCH_OPERATION.OPERATION_TYPE.eq(operationType));
        }
        if (operatorId != null) {
            query.and(BATCH_OPERATION.OPERATOR_ID.eq(operatorId));
        }
        query.orderBy(BATCH_OPERATION.CREATE_TIME.desc());
        Page<BatchOperation> page = batchOperationMapper.paginate(Page.of(pageNum, pageSize), query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    private void saveDetail(Long batchId, String batchNo, Integer targetType, Long targetId, String targetNo,
                            Integer resultStatus, String failReason, String oldValue, String newValue) {
        BatchOperationDetail detail = new BatchOperationDetail();
        detail.setBatchId(batchId);
        detail.setBatchNo(batchNo);
        detail.setTargetType(targetType);
        detail.setTargetId(targetId);
        detail.setTargetNo(targetNo);
        detail.setResultStatus(resultStatus);
        detail.setFailReason(failReason);
        detail.setOldValue(oldValue);
        detail.setNewValue(newValue);
        detail.setCreateTime(LocalDateTime.now());
        batchOperationDetailMapper.insert(detail);
    }

    private String getAttrValue(Room room, String attrType) {
        switch (attrType) {
            case "orientation":
                return room.getOrientation();
            case "viewType":
                return room.getViewType();
            case "locationFeatures":
                return room.getLocationFeatures();
            case "specialTags":
                return room.getSpecialTags();
            default:
                return null;
        }
    }

    private void setAttrValue(Room room, String attrType, String value) {
        switch (attrType) {
            case "orientation":
                room.setOrientation(value);
                break;
            case "viewType":
                room.setViewType(value);
                break;
            case "locationFeatures":
                room.setLocationFeatures(value);
                break;
            case "specialTags":
                room.setSpecialTags(value);
                break;
        }
    }

    private String calculateNewAttrValue(String oldValue, Integer mode, String attrValue) {
        if (mode == 1) {
            return attrValue;
        }
        if (mode == 3) {
            if (!StringUtils.hasText(oldValue)) {
                return "";
            }
            Set<String> oldSet = new HashSet<>(Arrays.asList(oldValue.split(",")));
            Set<String> removeSet = new HashSet<>(Arrays.asList(attrValue.split(",")));
            oldSet.removeAll(removeSet);
            return String.join(",", oldSet);
        }
        if (mode == 2) {
            Set<String> resultSet = new HashSet<>();
            if (StringUtils.hasText(oldValue)) {
                resultSet.addAll(Arrays.asList(oldValue.split(",")));
            }
            if (StringUtils.hasText(attrValue)) {
                resultSet.addAll(Arrays.asList(attrValue.split(",")));
            }
            return String.join(",", resultSet);
        }
        return oldValue;
    }
}
