package com.example.Badminton.service;

import com.example.Badminton.dto.CustomerDTO;
import com.example.Badminton.entity.Customer;
import com.example.Badminton.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<CustomerDTO> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(customer -> new CustomerDTO(
                        customer.getId(),
                        customer.getName(),
                        customer.getEmail(),
                        customer.getPhone(),
                        customer.getAddress(),
                        customer.getRole()))
                .collect(Collectors.toList());
    }

    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Optional<Customer> findByGoogleId(String googleId) {
        return customerRepository.findByGoogleId(googleId);
    }

    public Optional<Customer> findByGoogleIdAndProvider(String googleId, String provider) {
        return customerRepository.findByGoogleIdAndProvider(googleId, provider);
    }

    public Customer findOrCreateCustomer(String providerId, String name, String email, String picture, String provider) {
        try {
            System.out.println("findOrCreateCustomer: providerId=" + providerId + ", email=" + email + ", provider=" + provider);
            Optional<Customer> existingCustomer = customerRepository.findByGoogleIdAndProvider(providerId, provider);
            if (existingCustomer.isPresent()) {
                System.out.println("Found existing customer: " + existingCustomer.get().getEmail());
                return existingCustomer.get();
            }

            Optional<Customer> customerByEmail = customerRepository.findByEmail(email);
            if (customerByEmail.isPresent()) {
                Customer customer = customerByEmail.get();
                customer.setGoogleId(providerId);
                customer.setProvider(provider);
                customer.setPicture(picture);
                System.out.println("Updating customer with email: " + email);
                return customerRepository.save(customer);
            }

            Customer newCustomer = new Customer();
            newCustomer.setGoogleId(providerId);
            newCustomer.setName(name);
            newCustomer.setEmail(email);
            newCustomer.setPicture(picture);
            newCustomer.setRole("USER");
            newCustomer.setCreatedAt(LocalDateTime.now());
            newCustomer.setProvider(provider);
            System.out.println("Creating new customer: " + email);
            return customerRepository.save(newCustomer);
        } catch (DataIntegrityViolationException e) {
            System.err.println("Database error: Duplicate email or constraint violation - " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Email already exists: " + email);
        } catch (Exception e) {
            System.err.println("Error in findOrCreateCustomer: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create or find customer: " + e.getMessage(), e);
        }
    }


    public boolean isProfileComplete(Customer customer) {
        return customer.getPhone() != null && customer.getAddress() != null;
    }

    public Customer save(Customer customer) {
        try {
            return customerRepository.save(customer);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu khách hàng: " + e.getMessage(), e);
        }
    }
}