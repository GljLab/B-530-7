package com.example.permission.service;

import com.example.permission.common.BusinessException;
import com.example.permission.entity.*;
import com.example.permission.mapper.*;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.MaintenanceAssignRuleTableDef.MAINTENANCE_ASSIGN_RULE;
import static com.example.permission.entity.table.MaintenanceOrderTableDef.MAINTENANCE_ORDER;
import static com.example.permission.entity.table.MaintenanceStaffSkillTableDef.MAINTENANCE_STAFF_SKILL;
import static com.example.permission.entity.table.SysRoleTableDef.SYS_ROLE;
import static com.example.permission.entity.table.SysUserRoleTableDef.SYS_USER_ROLE;

@Service
public class MaintenanceAssignService {

    @Autowired
    private MaintenanceOrderMapper maintenanceOrderMapper;

    @Autowired
    private MaintenanceAssignRuleMapper maintenanceAssignRuleMapper;

    @Autowired
    private MaintenanceStaffSkillMapper maintenanceStaffSkillMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private FloorMapper floorMapper;

    @Autowired
    private MaintenanceOrderService maintenanceOrderService;

    private static final Map<Integer, String> MAINTENANCE_TYPE_MAP = MaintenanceOrderService.getTypeMap();

    private static final String MAINTENANCE_STAFF_ROLE_KEY = "maintenance_staff";

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> autoAssign(Long orderId, Long operatorId, String operatorName) {
        Map<String, Object> result = new HashMap<>();
        MaintenanceOrder order = maintenanceOrderMapper.selectOneById(orderId);
        if (order == null) {
            throw new BusinessException("维护单不存在");
        }
        if (order.getStatus() != 1) {
            throw new BusinessException("当前状态不允许自动分配");
        }

        List<Long> candidateUserIds = findCandidateUsersByRules(order);

        if (candidateUserIds.isEmpty()) {
            candidateUserIds = getAllMaintenanceStaffIds();
        }

        if (candidateUserIds.isEmpty()) {
            throw new BusinessException("未找到可用的维修人员");
        }

        Long selectedUserId = selectBestStaff(candidateUserIds, order);

        maintenanceOrderService.assign(orderId, selectedUserId, operatorId, operatorName);

        SysUser selectedUser = sysUserMapper.selectOneById(selectedUserId);
        result.put("success", true);
        result.put("orderId", orderId);
        result.put("assignedUserId", selectedUserId);
        result.put("assignedUserName", selectedUser != null ?
                (selectedUser.getNickname() != null ? selectedUser.getNickname() : selectedUser.getUsername()) : "");
        result.put("message", "分配成功");

        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchAutoAssign(List<Long> orderIds, Long operatorId, String operatorName) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failList = new ArrayList<>();

        for (Long orderId : orderIds) {
            try {
                Map<String, Object> assignResult = autoAssign(orderId, operatorId, operatorName);
                successList.add(assignResult);
            } catch (Exception e) {
                Map<String, Object> failItem = new HashMap<>();
                failItem.put("orderId", orderId);
                failItem.put("message", e.getMessage());
                failList.add(failItem);
            }
        }

        result.put("totalCount", orderIds.size());
        result.put("successCount", successList.size());
        result.put("failCount", failList.size());
        result.put("successList", successList);
        result.put("failList", failList);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchAssign(List<Long> orderIds, Long targetUserId, Long operatorId, String operatorName) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failList = new ArrayList<>();

        SysUser targetUser = sysUserMapper.selectOneById(targetUserId);
        if (targetUser == null) {
            throw new BusinessException("目标维修人员不存在");
        }
        String targetUserName = targetUser.getNickname() != null ? targetUser.getNickname() : targetUser.getUsername();

        for (Long orderId : orderIds) {
            try {
                maintenanceOrderService.assign(orderId, targetUserId, operatorId, operatorName);
                Map<String, Object> item = new HashMap<>();
                item.put("orderId", orderId);
                item.put("assignedUserId", targetUserId);
                item.put("assignedUserName", targetUserName);
                successList.add(item);
            } catch (Exception e) {
                Map<String, Object> failItem = new HashMap<>();
                failItem.put("orderId", orderId);
                failItem.put("message", e.getMessage());
                failList.add(failItem);
            }
        }

        result.put("totalCount", orderIds.size());
        result.put("successCount", successList.size());
        result.put("failCount", failList.size());
        result.put("targetUserId", targetUserId);
        result.put("targetUserName", targetUserName);
        result.put("successList", successList);
        result.put("failList", failList);
        return result;
    }

    public List<Map<String, Object>> getStaffWorkload() {
        List<Long> staffIds = getAllMaintenanceStaffIds();
        List<Map<String, Object>> result = new ArrayList<>();

        QueryWrapper processingQuery = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.STATUS.in(Arrays.asList(1, 2)))
                .and(MAINTENANCE_ORDER.ASSIGNED_USER_ID.isNotNull())
                .and(MAINTENANCE_ORDER.DELETED.eq(0));
        List<MaintenanceOrder> processingOrders = maintenanceOrderMapper.selectListByQuery(processingQuery);

        Map<Long, List<MaintenanceOrder>> processingGroup = processingOrders.stream()
                .collect(Collectors.groupingBy(MaintenanceOrder::getAssignedUserId));

        QueryWrapper pendingQuery = QueryWrapper.create()
                .from(MaintenanceOrder.class)
                .where(MAINTENANCE_ORDER.STATUS.eq(1))
                .and(MAINTENANCE_ORDER.DELETED.eq(0));
        long pendingUnassigned = maintenanceOrderMapper.selectCountByQuery(pendingQuery);

        for (Long staffId : staffIds) {
            SysUser user = sysUserMapper.selectOneById(staffId);
            if (user == null) continue;

            List<MaintenanceOrder> staffOrders = processingGroup.getOrDefault(staffId, Collections.emptyList());
            long processingCount = staffOrders.stream().filter(o -> o.getStatus() == 2).count();
            long assignedPendingCount = staffOrders.stream().filter(o -> o.getStatus() == 1).count();

            Map<String, Object> item = new HashMap<>();
            item.put("userId", user.getId());
            item.put("username", user.getUsername());
            item.put("nickname", user.getNickname());
            item.put("avatar", user.getAvatar());
            item.put("status", user.getStatus());
            item.put("processingCount", processingCount);
            item.put("assignedPendingCount", assignedPendingCount);
            item.put("totalWorkload", processingCount + assignedPendingCount);

            List<MaintenanceStaffSkill> skills = listStaffSkills(staffId);
            List<Map<String, Object>> skillList = new ArrayList<>();
            for (MaintenanceStaffSkill s : skills) {
                Map<String, Object> skillMap = new HashMap<>();
                skillMap.put("skillType", s.getSkillType());
                skillMap.put("skillTypeName", MAINTENANCE_TYPE_MAP.getOrDefault(
                        Integer.parseInt(s.getSkillType()), s.getSkillType()));
                skillMap.put("skillLevel", s.getSkillLevel());
                skillList.add(skillMap);
            }
            item.put("skills", skillList);

            result.add(item);
        }

        result.sort((a, b) -> Long.compare(
                (Long) b.get("totalWorkload"),
                (Long) a.get("totalWorkload")
        ));

        Map<String, Object> summary = new HashMap<>();
        summary.put("pendingUnassigned", pendingUnassigned);
        result.add(0, summary);

        return result;
    }

    public List<Map<String, Object>> getStaffWithSkills() {
        List<Long> staffIds = getAllMaintenanceStaffIds();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Long staffId : staffIds) {
            SysUser user = sysUserMapper.selectOneById(staffId);
            if (user == null) continue;

            Map<String, Object> item = new HashMap<>();
            item.put("userId", user.getId());
            item.put("username", user.getUsername());
            item.put("nickname", user.getNickname());
            item.put("avatar", user.getAvatar());
            item.put("status", user.getStatus());

            List<MaintenanceStaffSkill> skills = listStaffSkills(staffId);
            List<Map<String, Object>> skillList = new ArrayList<>();
            for (MaintenanceStaffSkill s : skills) {
                Map<String, Object> skillMap = new HashMap<>();
                skillMap.put("id", s.getId());
                skillMap.put("skillType", s.getSkillType());
                skillMap.put("skillTypeName", MAINTENANCE_TYPE_MAP.getOrDefault(
                        Integer.parseInt(s.getSkillType()), s.getSkillType()));
                skillMap.put("skillLevel", s.getSkillLevel());
                skillList.add(skillMap);
            }
            item.put("skills", skillList);

            QueryWrapper workloadQuery = QueryWrapper.create()
                    .from(MaintenanceOrder.class)
                    .where(MAINTENANCE_ORDER.ASSIGNED_USER_ID.eq(staffId))
                    .and(MAINTENANCE_ORDER.STATUS.in(Arrays.asList(1, 2)))
                    .and(MAINTENANCE_ORDER.DELETED.eq(0));
            long workload = maintenanceOrderMapper.selectCountByQuery(workloadQuery);
            item.put("workload", workload);

            result.add(item);
        }

        result.sort((a, b) -> {
            String nameA = (String) a.get("nickname");
            String nameB = (String) b.get("nickname");
            if (nameA == null) nameA = (String) a.get("username");
            if (nameB == null) nameB = (String) b.get("username");
            return nameA.compareTo(nameB);
        });

        return result;
    }

    public List<MaintenanceAssignRule> listRules() {
        QueryWrapper query = QueryWrapper.create()
                .from(MaintenanceAssignRule.class)
                .orderBy(MAINTENANCE_ASSIGN_RULE.RULE_TYPE.asc(),
                        MAINTENANCE_ASSIGN_RULE.PRIORITY.asc(),
                        MAINTENANCE_ASSIGN_RULE.CREATE_TIME.desc());
        return maintenanceAssignRuleMapper.selectListByQuery(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public MaintenanceAssignRule saveRule(MaintenanceAssignRule rule) {
        if (rule.getId() == null) {
            rule.setCreateTime(LocalDateTime.now());
            rule.setUpdateTime(LocalDateTime.now());
            if (rule.getStatus() == null) {
                rule.setStatus(1);
            }
            if (rule.getPriority() == null) {
                rule.setPriority(1);
            }
            maintenanceAssignRuleMapper.insert(rule);
        } else {
            MaintenanceAssignRule existing = maintenanceAssignRuleMapper.selectOneById(rule.getId());
            if (existing == null) {
                throw new BusinessException("规则不存在");
            }
            rule.setUpdateTime(LocalDateTime.now());
            rule.setCreateTime(existing.getCreateTime());
            maintenanceAssignRuleMapper.update(rule);
        }
        return rule;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteRule(Long id) {
        MaintenanceAssignRule rule = maintenanceAssignRuleMapper.selectOneById(id);
        if (rule == null) {
            throw new BusinessException("规则不存在");
        }
        maintenanceAssignRuleMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateRuleStatus(Long id, Integer status) {
        MaintenanceAssignRule rule = maintenanceAssignRuleMapper.selectOneById(id);
        if (rule == null) {
            throw new BusinessException("规则不存在");
        }
        rule.setStatus(status);
        rule.setUpdateTime(LocalDateTime.now());
        maintenanceAssignRuleMapper.update(rule);
    }

    public List<MaintenanceStaffSkill> listStaffSkills(Long userId) {
        QueryWrapper query = QueryWrapper.create()
                .from(MaintenanceStaffSkill.class)
                .where(MAINTENANCE_STAFF_SKILL.USER_ID.eq(userId))
                .orderBy(MAINTENANCE_STAFF_SKILL.SKILL_LEVEL.desc());
        return maintenanceStaffSkillMapper.selectListByQuery(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public MaintenanceStaffSkill saveStaffSkill(MaintenanceStaffSkill skill) {
        if (skill.getId() == null) {
            skill.setCreateTime(LocalDateTime.now());
            maintenanceStaffSkillMapper.insert(skill);
        } else {
            MaintenanceStaffSkill existing = maintenanceStaffSkillMapper.selectOneById(skill.getId());
            if (existing == null) {
                throw new BusinessException("专长记录不存在");
            }
            skill.setCreateTime(existing.getCreateTime());
            maintenanceStaffSkillMapper.update(skill);
        }
        return skill;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteStaffSkill(Long id) {
        MaintenanceStaffSkill skill = maintenanceStaffSkillMapper.selectOneById(id);
        if (skill == null) {
            throw new BusinessException("专长记录不存在");
        }
        maintenanceStaffSkillMapper.deleteById(id);
    }

    private List<Long> findCandidateUsersByRules(MaintenanceOrder order) {
        Set<Long> candidateUsers = new LinkedHashSet<>();

        QueryWrapper ruleQuery = QueryWrapper.create()
                .from(MaintenanceAssignRule.class)
                .where(MAINTENANCE_ASSIGN_RULE.STATUS.eq(1))
                .orderBy(MAINTENANCE_ASSIGN_RULE.RULE_TYPE.asc(),
                        MAINTENANCE_ASSIGN_RULE.PRIORITY.asc());
        List<MaintenanceAssignRule> allRules = maintenanceAssignRuleMapper.selectListByQuery(ruleQuery);

        Integer maintenanceType = order.getMaintenanceType();
        List<MaintenanceAssignRule> typeRules = allRules.stream()
                .filter(r -> r.getRuleType() != null && r.getRuleType() == 1)
                .collect(Collectors.toList());
        for (MaintenanceAssignRule rule : typeRules) {
            if (rule.getConditionValue() != null && maintenanceType != null) {
                try {
                    if (Integer.parseInt(rule.getConditionValue()) == maintenanceType) {
                        addRuleTargetUsers(rule, candidateUsers);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (!candidateUsers.isEmpty()) {
            return new ArrayList<>(candidateUsers);
        }

        Integer floorNumber = getFloorNumberByRoomId(order.getRoomId());
        if (floorNumber != null) {
            List<MaintenanceAssignRule> floorRules = allRules.stream()
                    .filter(r -> r.getRuleType() != null && r.getRuleType() == 2)
                    .collect(Collectors.toList());
            for (MaintenanceAssignRule rule : floorRules) {
                if (isFloorInRange(rule.getConditionValue(), floorNumber)) {
                    addRuleTargetUsers(rule, candidateUsers);
                }
            }
        }

        if (!candidateUsers.isEmpty()) {
            return new ArrayList<>(candidateUsers);
        }

        List<MaintenanceAssignRule> priorityRules = allRules.stream()
                .filter(r -> r.getRuleType() != null && r.getRuleType() == 3)
                .collect(Collectors.toList());
        for (MaintenanceAssignRule rule : priorityRules) {
            if (rule.getConditionValue() != null && order.getPriority() != null) {
                try {
                    if (Integer.parseInt(rule.getConditionValue()) == order.getPriority()) {
                        addRuleTargetUsers(rule, candidateUsers);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return new ArrayList<>(candidateUsers);
    }

    private void addRuleTargetUsers(MaintenanceAssignRule rule, Set<Long> candidateUsers) {
        if (rule.getTargetUserId() != null) {
            SysUser user = sysUserMapper.selectOneById(rule.getTargetUserId());
            if (user != null && user.getStatus() != null && user.getStatus() == 1) {
                candidateUsers.add(rule.getTargetUserId());
            }
        }
        if (StringUtils.hasText(rule.getTargetRoleKey())) {
            List<Long> userIds = getUserIdsByRoleKey(rule.getTargetRoleKey());
            candidateUsers.addAll(userIds);
        }
    }

    private List<Long> getUserIdsByRoleKey(String roleKey) {
        List<Long> result = new ArrayList<>();

        QueryWrapper roleQuery = QueryWrapper.create()
                .from(SysRole.class)
                .where(SYS_ROLE.ROLE_KEY.eq(roleKey))
                .and(SYS_ROLE.STATUS.eq(1))
                .limit(1);
        SysRole role = sysRoleMapper.selectOneByQuery(roleQuery);

        if (role != null) {
            QueryWrapper userRoleQuery = QueryWrapper.create()
                    .from(SysUserRole.class)
                    .where(SYS_USER_ROLE.ROLE_ID.eq(role.getId()));
            List<SysUserRole> userRoles = sysUserRoleMapper.selectListByQuery(userRoleQuery);

            for (SysUserRole ur : userRoles) {
                SysUser user = sysUserMapper.selectOneById(ur.getUserId());
                if (user != null && user.getStatus() != null && user.getStatus() == 1) {
                    result.add(user.getId());
                }
            }
        }

        return result;
    }

    private List<Long> getAllMaintenanceStaffIds() {
        return getUserIdsByRoleKey(MAINTENANCE_STAFF_ROLE_KEY);
    }

    private Integer getFloorNumberByRoomId(Long roomId) {
        if (roomId == null) return null;
        Room room = roomMapper.selectOneById(roomId);
        if (room == null || room.getFloorId() == null) return null;
        Floor floor = floorMapper.selectOneById(room.getFloorId());
        return floor != null ? floor.getFloorNumber() : null;
    }

    private boolean isFloorInRange(String conditionValue, Integer floorNumber) {
        if (!StringUtils.hasText(conditionValue) || floorNumber == null) {
            return false;
        }
        try {
            if (conditionValue.contains("-")) {
                String[] parts = conditionValue.split("-");
                if (parts.length == 2) {
                    int start = Integer.parseInt(parts[0].trim());
                    int end = Integer.parseInt(parts[1].trim());
                    return floorNumber >= start && floorNumber <= end;
                }
            } else {
                int singleFloor = Integer.parseInt(conditionValue.trim());
                return floorNumber == singleFloor;
            }
        } catch (NumberFormatException ignored) {
        }
        return false;
    }

    private Long selectBestStaff(List<Long> candidateUserIds, MaintenanceOrder order) {
        Map<Long, Long> workloadMap = new HashMap<>();
        for (Long userId : candidateUserIds) {
            QueryWrapper query = QueryWrapper.create()
                    .from(MaintenanceOrder.class)
                    .where(MAINTENANCE_ORDER.ASSIGNED_USER_ID.eq(userId))
                    .and(MAINTENANCE_ORDER.STATUS.in(Arrays.asList(1, 2)))
                    .and(MAINTENANCE_ORDER.DELETED.eq(0));
            long count = maintenanceOrderMapper.selectCountByQuery(query);
            workloadMap.put(userId, count);
        }

        long minWorkload = workloadMap.values().stream()
                .mapToLong(Long::longValue)
                .min()
                .orElse(0);

        List<Long> lowestWorkloadUsers = workloadMap.entrySet().stream()
                .filter(e -> e.getValue() == minWorkload)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (lowestWorkloadUsers.size() == 1) {
            return lowestWorkloadUsers.get(0);
        }

        Map<Long, Integer> skillScoreMap = new HashMap<>();
        for (Long userId : lowestWorkloadUsers) {
            int score = 0;
            List<MaintenanceStaffSkill> skills = listStaffSkills(userId);
            if (order.getMaintenanceType() != null) {
                String typeStr = String.valueOf(order.getMaintenanceType());
                for (MaintenanceStaffSkill skill : skills) {
                    if (typeStr.equals(skill.getSkillType())) {
                        score += skill.getSkillLevel() != null ? skill.getSkillLevel() * 10 : 10;
                    }
                }
            }
            skillScoreMap.put(userId, score);
        }

        return skillScoreMap.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(lowestWorkloadUsers.get(0));
    }
}
