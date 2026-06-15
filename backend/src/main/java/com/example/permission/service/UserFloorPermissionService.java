package com.example.permission.service;

import com.example.permission.entity.*;
import com.example.permission.mapper.*;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.permission.entity.table.RoomTableDef.ROOM;

@Service
public class UserFloorPermissionService {

    @Autowired
    private UserFloorPermissionMapper userFloorPermissionMapper;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private BuildingMapper buildingMapper;

    @Autowired
    private FloorMapper floorMapper;

    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private SysUserService sysUserService;

    private static final Set<String> ADMIN_ROLES = new HashSet<>(Arrays.asList(
            "admin", "hotel_admin", "frontdesk_manager"
    ));

    public List<UserFloorPermission> getPermissionsByUserId(Long userId) {
        return userFloorPermissionMapper.selectByUserId(userId);
    }

    public List<Long> getFloorIdsByUserId(Long userId) {
        if (isAdminUser(userId)) {
            return null;
        }
        List<UserFloorPermission> permissions = getPermissionsByUserId(userId);
        if (permissions == null || permissions.isEmpty()) {
            return null;
        }
        return permissions.stream()
                .map(UserFloorPermission::getFloorId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public void savePermissions(Long userId, List<Long> floorIds) {
        clearPermissions(userId);
        if (floorIds == null || floorIds.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        for (Long floorId : floorIds) {
            Floor floor = floorMapper.selectOneById(floorId);
            if (floor == null) {
                continue;
            }
            UserFloorPermission permission = new UserFloorPermission();
            permission.setUserId(userId);
            permission.setBuildingId(floor.getBuildingId());
            permission.setFloorId(floorId);
            permission.setCreateTime(now);
            userFloorPermissionMapper.insert(permission);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void clearPermissions(Long userId) {
        userFloorPermissionMapper.deleteByUserId(userId);
    }

    public boolean checkUserHasFloorPermission(Long userId, Long floorId) {
        List<Long> floorIds = getFloorIdsByUserId(userId);
        if (floorIds == null) {
            return true;
        }
        return floorIds.contains(floorId);
    }

    public Map<String, Object> getUserPermissionSummary(Long userId) {
        Map<String, Object> result = new HashMap<>();
        SysUser user = sysUserService.getUserDetail(userId);
        if (user == null) {
            result.put("userId", userId);
            result.put("summary", "用户不存在");
            result.put("floorCount", 0);
            result.put("roomCount", 0);
            return result;
        }

        result.put("userId", userId);
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());

        if (isAdminUser(userId)) {
            result.put("isAdmin", true);
            result.put("summary", "全部楼层");
            long totalRooms = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class).where(ROOM.DELETED.eq(0))
            );
            result.put("floorCount", "全部");
            result.put("roomCount", totalRooms);
            return result;
        }

        result.put("isAdmin", false);
        List<UserFloorPermission> permissions = getPermissionsByUserId(userId);
        if (permissions == null || permissions.isEmpty()) {
            result.put("summary", "全部楼层（未配置权限）");
            long totalRooms = roomMapper.selectCountByQuery(
                    QueryWrapper.create().from(Room.class).where(ROOM.DELETED.eq(0))
            );
            result.put("floorCount", "全部");
            result.put("roomCount", totalRooms);
            return result;
        }

        Map<Long, List<Floor>> buildingFloorsMap = new LinkedHashMap<>();
        for (UserFloorPermission perm : permissions) {
            Floor floor = floorMapper.selectOneById(perm.getFloorId());
            if (floor != null) {
                buildingFloorsMap.computeIfAbsent(perm.getBuildingId(), k -> new ArrayList<>()).add(floor);
            }
        }

        List<String> summaryParts = new ArrayList<>();
        int totalRoomCount = 0;
        int totalFloorCount = 0;

        for (Map.Entry<Long, List<Floor>> entry : buildingFloorsMap.entrySet()) {
            Building building = buildingMapper.selectOneById(entry.getKey());
            String buildingName = building != null ? building.getBuildingName() : "未知楼栋";
            List<Floor> floors = entry.getValue();
            totalFloorCount += floors.size();

            List<Integer> floorNumbers = floors.stream()
                    .map(Floor::getFloorNumber)
                    .filter(Objects::nonNull)
                    .sorted()
                    .collect(Collectors.toList());

            String floorDesc;
            if (floorNumbers.size() == 1) {
                floorDesc = floorNumbers.get(0) + "层";
            } else {
                floorDesc = floorNumbers.get(0) + "-" + floorNumbers.get(floorNumbers.size() - 1) + "层";
            }
            summaryParts.add(buildingName + floorDesc);

            for (Floor floor : floors) {
                QueryWrapper roomQuery = QueryWrapper.create()
                        .from(Room.class)
                        .where(ROOM.FLOOR_ID.eq(floor.getId()))
                        .and(ROOM.DELETED.eq(0));
                totalRoomCount += (int) roomMapper.selectCountByQuery(roomQuery);
            }
        }

        result.put("summary", String.join("，", summaryParts) + "，共" + totalRoomCount + "个房间");
        result.put("floorCount", totalFloorCount);
        result.put("roomCount", totalRoomCount);
        return result;
    }

    public List<Map<String, Object>> getUsersWithFloorPermission() {
        List<Map<String, Object>> result = new ArrayList<>();
        List<Long> userIds = userFloorPermissionMapper.selectDistinctUserIds();
        for (Long userId : userIds) {
            Map<String, Object> userSummary = getUserPermissionSummary(userId);
            result.add(userSummary);
        }
        return result;
    }

    private boolean isAdminUser(Long userId) {
        SysUser user = sysUserService.getUserDetail(userId);
        if (user == null || user.getRoles() == null) {
            return false;
        }
        for (SysRole role : user.getRoles()) {
            if (ADMIN_ROLES.contains(role.getRoleKey())) {
                return true;
            }
        }
        return false;
    }
}
