package com.chadev.xcape.admin.controller;

import com.chadev.xcape.admin.controller.request.FakeReservationRequest;
import com.chadev.xcape.admin.controller.request.ReservationRegisterRequest;
import com.chadev.xcape.admin.service.BannerService;
import com.chadev.xcape.admin.service.ReservationService;
import com.chadev.xcape.admin.service.ThemeService;
import com.chadev.xcape.core.domain.dto.*;
import com.chadev.xcape.core.domain.request.ThemeModifyRequestDto;
import com.chadev.xcape.core.response.Response;
import com.chadev.xcape.core.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminRestController {

    private final CoreMerchantService coreMerchantService;
    private final CoreThemeService coreThemeService;
    private final ThemeService themeService;
    private final CorePriceService corePriceService;
    private final ReservationService reservationService;
    private final BannerService bannerService;
    private final CoreTimetableService coreTimetableService;

    @GetMapping("/merchants")
    public Response<List<MerchantDto>> getAllMerchantsWithThemes() {
        List<MerchantDto> merchantDtoList = coreMerchantService.getAllMerchantsWithThemes();
        return Response.success(merchantDtoList);
    }

    @GetMapping("/merchants/{merchantId}")
    public Response<MerchantDto> getMerchantById(@PathVariable Long merchantId) {
        MerchantDto merchantDto = coreMerchantService.getMerchantById(merchantId);
        return Response.success(merchantDto);
    }

    @GetMapping("/themes/{themeId}")
    public Response<ThemeDto> getThemeDetail(@PathVariable Long themeId) {
        ThemeDto theme = coreThemeService.getThemeDetail(themeId);
        return Response.success(theme);
    }

    @PostMapping("/merchants/{merchantId}/themes")
    public Response<Void> createThemeByMerchantId(@PathVariable Long merchantId, ThemeModifyRequestDto requestDto, List<PriceDto> priceDtoList,
                                                  MultipartHttpServletRequest request) throws IOException {
        themeService.createThemeByMerchantId(merchantId, requestDto, request, priceDtoList);
        return Response.success();
    }

    @PutMapping("/themes/{themeId}")
    public Response<Void> modifyThemeById(@PathVariable Long themeId, ThemeModifyRequestDto themeDto,
                                          MultipartHttpServletRequest request) throws IOException {
        themeService.modifyThemeDetail(themeId, themeDto, request);
        return Response.success();
    }

    @GetMapping("/themes/{themeId}/price")
    public Response<List<PriceDto>> getPriceListByThemeId(@PathVariable Long themeId) {
        List<PriceDto> priceListByThemeId = corePriceService.getPriceListByThemeId(themeId);
        return Response.success(priceListByThemeId);
    }

    @GetMapping("/themes/{themeId}/timetable")
    public Response<List<TimetableDto>> getTimetableListByThemeId(@PathVariable Long themeId) {
        List<TimetableDto> priceListByThemeId = coreTimetableService.getTimetableListByThemeId(themeId);
        return Response.success(priceListByThemeId);
    }

    // 지점별 빈 예약 생성
    @PostMapping("/reservation-batch")
    public Response<Void> reservationBatch(LocalDate date) {
        reservationService.reservationBatch(date);

        return Response.success();
    }

    // 예약 등록/수정
    @PutMapping("/reservations/{reservationId}")
    public Response<ReservationDto> registerReservation(@PathVariable String reservationId, @RequestBody ReservationRegisterRequest request) {
        ReservationDto savedReservation = reservationService.registerReservationById(reservationId, request);

        return Response.success(savedReservation);
    }

    @GetMapping("/reservations/{reservationId}")
    public Response<ReservationDto> getReservation(@PathVariable String reservationId) {
        ReservationDto reservationDto = reservationService.getReservation(reservationId);

        return Response.success(reservationDto);
    }

    // 예약 취소
    @DeleteMapping("/reservations/{reservationId}")
    public Response<Void> cancelReservation(@PathVariable String reservationId) {
        reservationService.cancelReservationById(reservationId);
        return Response.success();
    }

    // 가예약 등록
    @PutMapping("/reservations/fake")
    public Response<Void> registerFakeReservation(@RequestBody FakeReservationRequest request) {
        Long unreservedTime = request.getUnreservedTime();
        request.getReservationIdList().forEach((reservationId) -> reservationService.registerFakeReservation(reservationId, unreservedTime));

        return Response.success();
    }

    @PutMapping("/themes/{themeId}/price")
    public Response<Void> modifyPriceListByThemeId(@PathVariable Long themeId, @RequestBody List<PriceDto> priceDtoList) {
        corePriceService.modifyPriceListByThemeId(priceDtoList, themeId);
        return Response.success();
    }

    @DeleteMapping("/themes/{themeId}/price")
    public Response<Void> deletePriceList(@PathVariable Long themeId, @RequestBody List<PriceDto> priceDtoList) {
        corePriceService.deletePriceList(priceDtoList, themeId);
        return Response.success();
    }

    @GetMapping("/merchants/{merchantId}/banners")
    public Response<List<BannerDto>> getBannerListByMerchantId(@PathVariable Long merchantId) {
        List<BannerDto> bannerListByMerchantId = bannerService.getBannerListByMerchantId(merchantId);
        return Response.success(bannerListByMerchantId);
    }

    @PostMapping("/merchants/{merchantId}/banners")
    public Response<Void> createBannerByMerchantId(@PathVariable Long merchantId, BannerDto bannerDto,
                                                   MultipartHttpServletRequest request) throws IOException {
        bannerService.createBannerByMerchantId(merchantId, bannerDto, request);
        return Response.success();
    }

    @PutMapping("/merchants/{merchantId}/banners")
    public Response<Void> modifyBannerListByMerchantId(@PathVariable Long merchantId, @RequestBody List<BannerDto> bannerDtoList) {
        bannerService.modifyBannerListByMerchantId(merchantId, bannerDtoList);
        return Response.success();
    }

    @GetMapping("/banners/{bannerId}")
    public Response<BannerDto> getBannerDetail(@PathVariable Long bannerId) {
        BannerDto bannerDetail = bannerService.getBannerDetail(bannerId);
        return Response.success(bannerDetail);
    }

    @PutMapping("/banners/{bannerId}")
    public Response<Void> modifyBannerDetail(@PathVariable Long bannerId, BannerDto bannerDto, MultipartHttpServletRequest request) throws IOException {
        bannerService.modifyBannerDetail(bannerId, bannerDto, request);
        return Response.success();
    }
}
