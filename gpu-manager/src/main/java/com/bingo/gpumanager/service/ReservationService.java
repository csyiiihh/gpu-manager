package com.bingo.gpumanager.service;

import com.bingo.gpumanager.entity.GpuServer;
import com.bingo.gpumanager.entity.Reservation;
import com.bingo.gpumanager.enums.ReservationStatus;
import com.bingo.gpumanager.exception.BusinessException;
import com.bingo.gpumanager.mapper.GpuServerMapper;
import com.bingo.gpumanager.mapper.ReservationMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bingo.gpumanager.dto.ReservationDTO;
import com.bingo.gpumanager.dto.PageResult;
import com.bingo.gpumanager.enums.UserRole;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ReservationService{

    private final ReservationMapper reservationMapper;
    private final GpuServerMapper gpuServerMapper;

    public ReservationService(ReservationMapper reservationMapper, GpuServerMapper gpuServerMapper) {
        this.reservationMapper = reservationMapper;
        this.gpuServerMapper = gpuServerMapper;
    }

    @Transactional
    public void createReservation(Reservation reservation){

        Long serverId = reservation.getServerId();

        Long lockedServerId = reservationMapper.lockServer(serverId);

        if(lockedServerId == null){
            throw new BusinessException(404, "GPU服务器不存在");
        }

        LocalDateTime startTime = reservation.getStartTime();
        LocalDateTime endTime = reservation.getEndTime();

        LocalDateTime now = LocalDateTime.now();

        if(startTime.isBefore(now)){
            throw new BusinessException(
                    400,
                    "不能预约过去的时间"
            );
        }

        if(startTime == null || endTime == null){
            throw new BusinessException("预约时间不能为空");
        }

        if(!startTime.isBefore(endTime)){
            throw new BusinessException("开始时间必须早于结束时间");
        }

        int conflictCount =
                reservationMapper.countConflict(
                        reservation.getServerId(),
                        startTime,
                        endTime
                );

        if(conflictCount > 0){

            log.warn(
                    "预约冲突，userId={}，serverId={}，startTime={}，endTime={}",
                    reservation.getUserId(),
                    reservation.getServerId(),
                    startTime,
                    endTime
            );

            throw new BusinessException(409, "该时间段已被预约");
        }

        reservation.setStatus(ReservationStatus.ACTIVE.name());

        int result = reservationMapper.insert(reservation);

        if(result == 0){
            throw new BusinessException(500, "预约创建失败");
        }


        log.info(
                "预约创建成功，userId={}，serverId={}，startTime={}，endTime={}",
                reservation.getUserId(),
                reservation.getServerId(),
                reservation.getStartTime(),
                reservation.getEndTime()
        );
    }

    public List<Reservation> getReservationsByUserId(Long userId){
        return reservationMapper.findByUserId(userId);
    }

    public void cancelReservation(Long id, Long userId){

        Reservation reservation = reservationMapper.findById(id);

        if(reservation == null){
            throw new BusinessException(404, "预约记录不存在");
        }

        if(!reservation.getServerId().equals(userId)){
            throw new BusinessException(403, "无权取消该预约");
        }

        if(ReservationStatus.CANCELLED.name().equals(reservation.getStatus())){

            log.info(
                    "预约取消成功，reservationId={}，userId={}",
                    id,
                    userId
            );

            throw new BusinessException("该预约已取消");
        }

        reservationMapper.cancelById(id);
    }

    public List<ReservationDTO> getAllReservations() {
        return reservationMapper.findAllDetail();
    }

    public List<ReservationDTO> getMyReservations(Long userId) {
        return reservationMapper.findDetailByUserId(userId);
    }

    public PageResult<ReservationDTO> getReservationPage(
            Integer page,
            Integer size,
            String status,
            String username
    ){

        if(page == null || page < 1){
            page = 1;
        }

        if(size == null || size < 1){
            size = 10;
        }

        if(size > 100){
            size = 100;
        }

        int offset = (page - 1) * size;

        long total = reservationMapper.countReservations(
                status,
                username
        );

        List<ReservationDTO> records =
                reservationMapper.findPage(
                        status,
                        username,
                        offset,
                        size
                );

        PageResult<ReservationDTO> result =
                new PageResult<>();

        result.setTotal(total);
        result.setPage(page);
        result.setRecords(records);
        result.setSize(size);

        return result;
    }
}