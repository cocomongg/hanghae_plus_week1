package io.hhplus.tdd.point.controller;

import io.hhplus.tdd.common.response.ApiResponse;
import io.hhplus.tdd.point.dto.PointDto.ChargeRequest;
import io.hhplus.tdd.point.dto.PointDto.PointDetail;
import io.hhplus.tdd.point.dto.PointDto.PointHistoryDetail;
import io.hhplus.tdd.point.dto.PointDto.UseRequest;
import io.hhplus.tdd.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);

    private final PointService pointService;

    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    public ApiResponse<PointDetail> point(
            @PathVariable long id
    ) {
        PointDetail result = pointService.getUserPoint(id);
        return ApiResponse.defaultOk(result);
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public ApiResponse<List<PointHistoryDetail>> history(
            @PathVariable long id
    ) {
        List<PointHistoryDetail> results = pointService.getUserPointHistories(id);
        return ApiResponse.defaultOk(results);
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    public ApiResponse<PointDetail> charge(
            @PathVariable long id,
            @RequestBody ChargeRequest request
    ) {
        PointDetail result = pointService.charge(id, request.getAmount());
        return ApiResponse.defaultOk(result);
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    public ApiResponse<PointDetail> use(
            @PathVariable long id,
            @RequestBody UseRequest request
    ) {
        PointDetail result = pointService.use(id, request.getAmount());
        return ApiResponse.defaultOk(result);
    }
}
