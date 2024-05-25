package id.ac.ui.cs.advprog.serviceaccounts.controller.admin;

import id.ac.ui.cs.advprog.serviceaccounts.controller.AdminController;
import id.ac.ui.cs.advprog.serviceaccounts.model.Role;
import id.ac.ui.cs.advprog.serviceaccounts.model.User;
import id.ac.ui.cs.advprog.serviceaccounts.repository.UserRepository;
import id.ac.ui.cs.advprog.serviceaccounts.service.admin.AdminService;
import id.ac.ui.cs.advprog.serviceaccounts.service.admin.AdminServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminControllerTest {

    @Mock
    private AdminService adminService = new AdminServiceImpl();

    private AdminController adminController;

    private User testUser;

    @Mock
    private UserRepository userRepository;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        adminController = new AdminController(adminService);
        testUser = User.builder()
                .id(UUID.randomUUID())
                .name("testuser")
                .email("testuser@test.com")
                .username("testuser")
                .password("password")
                .role(Role.ADMIN)
                .active(true)
                .isVerified(true)
                .build();
    }

    @Test
    void testGetAllUsers() {
        List<User> users = Arrays.asList(testUser);
        when(adminService.getAllUsers()).thenReturn(users);
        ResponseEntity<List<User>> response = adminController.getAllUsers();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(users, response.getBody());
        verify(adminService, times(1)).getAllUsers();
    }

    @Test
    void testDeactivateUserSuccess() {
        UUID userId = testUser.getId();
        doNothing().when(adminService).deactivateUser(userId);
        ResponseEntity<Void> response = adminController.deactivateUser(userId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(adminService, times(1)).deactivateUser(userId);
    }

    @Test
    void testDeactivateUserNotFound() {
        UUID userId = UUID.randomUUID();
        doThrow(NoSuchElementException.class).when(adminService).deactivateUser(userId);
        ResponseEntity<Void> response = adminController.deactivateUser(userId);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(adminService, times(1)).deactivateUser(userId);
    }


}
