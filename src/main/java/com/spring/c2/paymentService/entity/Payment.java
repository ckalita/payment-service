package com.spring.c2.paymentService.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "payment-table")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Payment implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int paymentId;
	private String transactionId;
	private int orderId;
	private double amount;
	private String status;
	
}
