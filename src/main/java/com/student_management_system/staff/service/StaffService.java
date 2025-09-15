package com.student_management_system.staff.service;

import com.student_management_system.staff.model.Fee;
import com.student_management_system.staff.repository.FeeRepository;
import org.springframework.stereotype.Service;
import com.student_management_system.staff.dto.RecordPaymentDto;
import com.student_management_system.staff.model.FeeStatus;
import com.student_management_system.staff.model.Payment;
import com.student_management_system.staff.repository.PaymentRepository;
import com.student_management_system.user_management.model.User;
import com.student_management_system.user_management.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import com.student_management_system.staff.model.Event;
import com.student_management_system.staff.repository.EventRepository;
import com.student_management_system.staff.model.EventRegistration;
import com.student_management_system.staff.repository.EventRegistrationRepository;
import com.student_management_system.staff.model.Booking;
import com.student_management_system.staff.model.Facility;
import com.student_management_system.staff.repository.BookingRepository;
import com.student_management_system.staff.repository.FacilityRepository;
import com.student_management_system.user_management.model.User;

import java.time.LocalDateTime;

import java.time.LocalDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;

@Service
public class StaffService {

    private final FeeRepository feeRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final FacilityRepository facilityRepository;
    private final BookingRepository bookingRepository;

    public StaffService(FeeRepository feeRepository, PaymentRepository paymentRepository, UserRepository userRepository, EventRepository eventRepository, EventRegistrationRepository eventRegistrationRepository, FacilityRepository facilityRepository, BookingRepository bookingRepository) {
        this.feeRepository = feeRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.facilityRepository = facilityRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Fee> getAllFees() {
        // For now, we fetch all fees. Later, we can add sorting and filtering.
        return feeRepository.findAll();
    }

    public Fee findFeeById(Long feeId) {
        return feeRepository.findById(feeId)
                .orElseThrow(() -> new RuntimeException("Fee not found with id: " + feeId));
    }

    private void updateFeeStatus(Fee fee) {
        // compareTo returns 0 if they are equal, -1 if first is less, 1 if first is greater
        int comparison = fee.getAmountPaid().compareTo(fee.getTotalAmount());

        if (comparison >= 0) {
            fee.setStatus(FeeStatus.PAID);
        } else if (fee.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
            fee.setStatus(FeeStatus.PARTIALLY_PAID);
        } else {
            fee.setStatus(FeeStatus.UNPAID);
        }
    }

    @Transactional
    public void recordPayment(Long feeId, RecordPaymentDto paymentDto) {
        // 1. Find the Fee entity in the database.
        Fee fee = findFeeById(feeId);
        // 2. Identify the staff member performing the action.
        User staffMember = getCurrentUser();

        // 3. Create a new Payment entity and populate its details.
        Payment payment = new Payment();
        payment.setFee(fee);
        payment.setAmount(paymentDto.getAmount());
        payment.setPaymentMethod(paymentDto.getPaymentMethod());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setRecordedBy(staffMember);
        // 4. Save the new Payment record to the database.
        paymentRepository.save(payment);

        // 5. Update the parent Fee entity's balance.
        BigDecimal newAmountPaid = fee.getAmountPaid().add(paymentDto.getAmount());
        fee.setAmountPaid(newAmountPaid);

        // 6. Automatically recalculate and update the Fee's status (UNPAID, PARTIALLY_PAID, PAID).
        updateFeeStatus(fee);

        // 7. Save the updated Fee entity back to the database.
        feeRepository.save(fee);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        return userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    @Transactional
    public void sendFeeReminder(Long feeId) {
        Fee fee = findFeeById(feeId);

        // A simple check to ensure we don't send reminders for paid fees
        if (fee.getStatus() == FeeStatus.PAID) {
            throw new IllegalStateException("Cannot send a reminder for a fully paid fee.");
        }

        // The core logic: update the date and save.
        fee.setLastReminderSent(LocalDate.now());

        feeRepository.save(fee);

        // In a real-world application, this is where you would trigger an email or SMS service.
        // For now, we'll just log it to the console.
        System.out.println("Reminder sent for Fee ID: " + feeId + " to Student: " + fee.getStudent().getUsername());
    }

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    public Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
    }

    // Handle a student's registration
    @Transactional
    public void registerStudentForEvent(Long eventId, User student) {
        Event event = findEventById(eventId);

        // Check if the event is full
        if (event.getRegistrations() != null && event.getRegistrations().size() >= event.getMaxAttendees()) {
            throw new IllegalStateException("This event is already full.");
        }

        // Check if the student is already registered
        boolean alreadyRegistered = event.getRegistrations() != null && event.getRegistrations().stream()
                .anyMatch(reg -> reg.getStudent().getId().equals(student.getId()));
        if (alreadyRegistered) {
            throw new IllegalStateException("You are already registered for this event.");
        }

        EventRegistration registration = new EventRegistration();
        registration.setEvent(event);
        registration.setStudent(student);
        registration.setRegistrationDate(LocalDateTime.now());

        eventRegistrationRepository.save(registration);
    }

    public List<Facility> getAllFacilities() {
        return facilityRepository.findAll();
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Facility createFacility(Facility facility) {
        return facilityRepository.save(facility);
    }

    @Transactional
    public Booking createBooking(Booking booking, User bookedBy) {
        // 1. Check for scheduling conflicts
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                booking.getFacility().getId(), booking.getStartTime(), booking.getEndTime());

        if (!overlappingBookings.isEmpty()) {
            throw new IllegalStateException("The selected time slot is already booked for this facility.");
        }

        // 2. Check that end time is after start time
        if (!booking.getEndTime().isAfter(booking.getStartTime())) {
            throw new IllegalStateException("Booking end time must be after the start time.");
        }

        // 3. Set the user who is booking and save
        booking.setBookedBy(bookedBy);
        return bookingRepository.save(booking);
    }
}