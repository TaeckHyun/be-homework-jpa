package com.springboot.order.controller;

import com.springboot.coffee.service.CoffeeService;
import com.springboot.order.dto.OrderPatchDto;
import com.springboot.order.dto.OrderPostDto;
import com.springboot.order.dto.OrderResponseDto;
import com.springboot.order.entity.Order;
import com.springboot.order.mapper.OrderMapper;
import com.springboot.order.service.OrderService;
import com.springboot.response.MultiResponseDto;
import com.springboot.response.PageInfo;
import com.springboot.response.SingleResponseDto;
import com.springboot.utils.UriCreator;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/v11/orders")
@Validated
public class OrderController {
    private final static String ORDER_DEFAULT_URL = "/v11/orders";
    private final OrderService orderService;
    private final OrderMapper mapper;

    public OrderController(OrderService orderService,
                           OrderMapper mapper) {
        this.orderService = orderService;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity postOrder(@Valid @RequestBody OrderPostDto orderPostDto) {
        Order order = orderService.createOrder(mapper.orderPostDtoToOrder(orderPostDto));
        URI location = UriCreator.createUri(ORDER_DEFAULT_URL, order.getOrderId());

        return ResponseEntity.created(location).build();
    }

    @PatchMapping("/{order-id}")
    public ResponseEntity patchOrder(@PathVariable("order-id") @Positive long orderId,
                                     @Valid @RequestBody OrderPatchDto orderPatchDto) {
        orderPatchDto.setOrderId(orderId);
        Order order = orderService.updateOrder(mapper.orderPatchDtoToOrder(orderPatchDto));

        return new ResponseEntity<>(null);
    }

    @GetMapping("/{order-id}")
    public ResponseEntity getOrder(@PathVariable("order-id") @Positive long orderId) {
        // TODO JPA 기능에 맞춰서 회원이 주문한 커피 정보를 ResponseEntity에 포함 시키세요.
        Order order = orderService.findOrder(orderId);

        return new ResponseEntity<>(new SingleResponseDto<>(
                mapper.orderToOrderResponseDto(order)), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity getOrders(@Positive @RequestParam int page,
                                    @Positive @RequestParam int size) {
        Page<Order> pageOrders = orderService.findOrders(page - 1, size);
        List<Order> orders = pageOrders.getContent();

        return new ResponseEntity<>(new MultiResponseDto<>(
                mapper.ordersToOrderResponseDtos(orders), pageOrders), HttpStatus.OK);
    }

    @DeleteMapping("/{order-id}")
    public ResponseEntity cancelOrder(@PathVariable("order-id") @Positive long orderId) {
        orderService.cancelOrder(orderId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
