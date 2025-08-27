package com.spring.c2.paymentService.service;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.spring.c2.paymentService.entity.Payment;
import com.spring.c2.paymentService.repository.PaymentRepository;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PaymentService {

	@Autowired
	PaymentRepository paymentRepository;
	
	@Autowired
	private CacheManager cacheManager;
	
	@Autowired
	private EmailService emailService;

	@PostConstruct
	public void logCacheManager() {
	    System.out.println("Cache Manager: " + cacheManager.getClass().getName());
	}
	
	//@CachePut(value = "products", key = "#payment.id")
	public Payment processPayment(Payment payment) {
		log.info("Entering: processPayment method");
		payment.setStatus(processPaymentVendor());
		payment.setTransactionId(UUID.randomUUID().toString());
		
		Payment savedPayment  = paymentRepository.save(payment);
		
		String body = "Hi Pallabi, Your Payment Processed Successfully for orderid :" + savedPayment.getOrderId() + " with Txn id : " +savedPayment.getTransactionId();
		
		String html = """
		        <html>
		            <body>
		                <h2 style='color:blue;'>Hello from Spring Boot!</h2>
		                <p>This is a <b>HTML email</b> sent using <i>JavaMailSender</i>.</p>
		                <hr/>
		                <p>Regards,<br/>Your App Team</p>
		            </body>
		        </html>
		        """;
		
		emailService.sendSimpleMail("pallabi123saikia@gmail.com", "Payment Processed Successfully", body);
		
		return savedPayment;
	}
	
	public List<Payment> getAllPayments() {
		return paymentRepository.findAll();
	}
	
	

	private String processPaymentVendor() {
		// TODO Auto-generated method stub
		return new Random().nextBoolean()?"success":"false";
	}

	
	@Cacheable(value = "payment", key = "#paymentId", unless = "#result == null")
	public Payment getPaymentsById(int paymentId) {
		log.info("getting from DB");
		Payment result = paymentRepository.findById(paymentId).orElse(null);

	    if (result != null) {
	        System.out.println(">>> Object class: " + result.getClass().getName());
	        System.out.println(">>> Classloader: " + result.getClass().getClassLoader());
	    }

	    return result;
	}
}
