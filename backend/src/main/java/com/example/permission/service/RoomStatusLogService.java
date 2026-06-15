package com.example.permission.service;

import com.example.permission.entity.RoomStatusLog;
import com.example.permission.mapper.RoomStatusLogMapper;
import com.mybatisflex.core.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.permission.entity.table.RoomStatusLogTableDef.ROOM_STATUS_LOG;

@Service
public class RoomStatusLogService {

    @Autowired
    private RoomStatusLogMapper roomStatusLogMapper;

    public List<RoomStatusLog> listByRoomId(Long roomId) {
        QueryWrapper query = QueryWrapper.create()
                .from(RoomStatusLog.class)
                .where(ROOM_STATUS_LOG.ROOM_ID.eq(roomId))
                .orderBy(ROOM_STATUS_LOG.CREATE_TIME.desc());
        return roomStatusLogMapper.selectListByQuery(query);
    }

    @Transactional(rollbackFor = Exception.class)
    public void add(RoomStatusLog log) {
        log.setCreateTime(LocalDateTime.now());
        roomStatusLogMapper.insert(log);
    }
}
