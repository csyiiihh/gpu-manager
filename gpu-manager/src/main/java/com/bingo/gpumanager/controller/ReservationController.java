package com.bingo.gpumanager.controller;


import com.bingo.gpumanager.common.Result;
import com.bingo.gpumanager.dto.CreateReservationRequest;
import com.bingo.gpumanager.dto.ReservationDTO;
import com.bingo.gpumanager.entity.Reservation;
import com.bingo.gpumanager.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import com.bingo.gpumanager.annotation.AdminOnly;
import com.bingo.gpumanager.dto.ReservationDTO;
import com.bingo.gpumanager.dto.PageResult;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@Tag(
        name = "GPU预约管理",
        description = "GPU预约创建、查询、取消及管理员查询接口"
)
@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService =reservationService;
    }

    @Operation(summary = "创建GPU预约")
    @PostMapping("/reservations")
    public Result<Void> createReservation(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateReservationRequest request) {

        Reservation reservation = new Reservation();

        reservation.setUserId(userId);
        reservation.setServerId(request.getServerId());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());

        reservationService.createReservation(reservation);

        return Result.success("预约成功", null);
    }

    @Operation(summary = "查看我的预约")
    @GetMapping("/reservations/my")
    public Result<List<ReservationDTO>> getReservationsByUser(@RequestAttribute("userId") Long userId){
        return Result.success(reservationService.getMyReservations(userId));
    }

    @Operation(summary = "取消预约")
    @PutMapping("/reservations/{id}/cancel")
    public Result<Void> cancelReservation(
            @PathVariable Long id,
            @RequestAttribute("userId") Long userId){

        reservationService.cancelReservation(id, userId);

        return Result.success("取消成功", null);
    }

//    @AdminOnly
//    @GetMapping("/reservations")
//    public Result<List<ReservationDTO>> getAllReservations(){
//        return Result.success(reservationService.getAllReservations());
//    }

    @Operation(summary = "管理员分页查询全部预约")
    @AdminOnly
    @GetMapping("/reservations")
    public Result<PageResult<ReservationDTO>> getReservations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String username) {

        return Result.success(
                reservationService.getReservationPage(
                        page,
                        size,
                        status,
                        username
                )
        );
    }
}