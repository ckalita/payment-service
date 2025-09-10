package com.spring.c2.paymentService.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.c2.paymentService.entity.Payment;
import com.spring.c2.paymentService.service.PaymentService;

import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {
	
	@Autowired
	PaymentService paymentService;

	@PostMapping(path = "/")
	@Observed(name = "user.name",
		contextualName = "OrderService",
		lowCardinalityKeyValues = {"userType", "userType2"}		
	)
	public ResponseEntity<Payment> processPayment(@RequestBody Payment payment){
		Payment paymentResponse =  paymentService.processPayment(payment);
		
		return new ResponseEntity<Payment>(paymentResponse, HttpStatus.OK);
	}
	
	@GetMapping(path = "/")
	public ResponseEntity<List<Payment>> getAllPayments(){
		log.info("Entering getAllPayments");
		List<Payment> paymentList = paymentService.getAllPayments();
		log.info("Exiting getAllPayments");
		return new ResponseEntity<List<Payment>>(paymentList, HttpStatus.OK);
	}
	
	@GetMapping(path = "/{id}")
	public ResponseEntity<Payment> getPaymentsById(@PathVariable int id){
		log.info("entering getPaymentsById");
		Payment payment = paymentService.getPaymentsById(id);
		log.info("exiting getPaymentsById");
		return payment != null ? new ResponseEntity<>(payment, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}
	
}
