package com.example.permission.service;

import com.example.permission.common.PageResult;
import com.example.permission.entity.RoomChangeLog;
import com.example.permission.entity.Room;
import com.example.permission.mapper.RoomChangeLogMapper;
import com.example.permission.mapper.RoomMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.RoomChangeLogTableDef.ROOM_CHANGE_LOG;

@Service
public class RoomChangeLogService {

    @Autowired
    private RoomChangeLogMapper roomChangeLogMapper;

    @Autowired
    private RoomMapper roomMapper;

    public void logRoomChange(Long roomId, Integer operationType, String changeField,
                              String oldValue, String newValue, String changeReason,
                              String relatedOrderNo, Long relatedOrderId,
                              Long operatorId, String operatorName,
                              String operator, String operatorRole, String terminalIp) {
        Room room = roomMapper.selectOneById(roomId);
        if (room == null) return;

        RoomChangeLog log = new RoomChangeLog();
        log.setRoomId(roomId);
        log.setRoomNumber(room.getRoomNumber());
        log.setOperationType(operationType);
        log.setChangeField(changeField);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setChangeReason(changeReason);
        log.setRelatedOrderNo(relatedOrderNo);
        log.setRelatedOrderId(relatedOrderId);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setOperator(operator);
        log.setOperatorRole(operatorRole);
        log.setTerminalType("PC");
        log.setTerminalIp(terminalIp);
        log.setCreateTime(LocalDateTime.now());
        roomChangeLogMapper.insert(log);
    }

    public PageResult<RoomChangeLog> pageList(Integer pageNum, Integer pageSize,
                                               String roomNumber, Long operatorId,
                                               LocalDateTime startTime, LocalDateTime endTime,
                                               List<Integer> operationTypes) {
        QueryWrapper query = QueryWrapper.create()
                .from(RoomChangeLog.class);
        if (StringUtils.hasText(roomNumber)) {
            query.and(ROOM_CHANGE_LOG.ROOM_NUMBER.like(roomNumber));
        }
        if (operatorId != null) {
            query.and(ROOM_CHANGE_LOG.OPERATOR_ID.eq(operatorId));
        }
        if (startTime != null) {
            query.and(ROOM_CHANGE_LOG.CREATE_TIME.ge(startTime));
        }
        if (endTime != null) {
            query.and(ROOM_CHANGE_LOG.CREATE_TIME.le(endTime));
        }
        if (operationTypes != null && !operationTypes.isEmpty()) {
            query.and(ROOM_CHANGE_LOG.OPERATION_TYPE.in(operationTypes));
        }
        query.orderBy(ROOM_CHANGE_LOG.CREATE_TIME.desc());
        Page<RoomChangeLog> page = roomChangeLogMapper.paginate(Page.of(pageNum, pageSize), query);
        return new PageResult<>(page.getTotalRow(), page.getRecords(),
                (long) page.getPageNumber(), (long) page.getPageSize());
    }

    public List<RoomChangeLog> listByRoomId(Long roomId) {
        QueryWrapper query = QueryWrapper.create()
                .from(RoomChangeLog.class)
                .where(ROOM_CHANGE_LOG.ROOM_ID.eq(roomId))
                .orderBy(ROOM_CHANGE_LOG.CREATE_TIME.desc());
        return roomChangeLogMapper.selectListByQuery(query);
    }
}
