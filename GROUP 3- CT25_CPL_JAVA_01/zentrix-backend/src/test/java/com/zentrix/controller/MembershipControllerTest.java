package com.zentrix.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zentrix.model.entity.Membership;
import com.zentrix.model.entity.Promotion;
import com.zentrix.model.request.MembershipRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.model.response.ResponseObject;
import com.zentrix.service.MembershipService;

import static org.junit.jupiter.api.Assertions.*;

/*
* @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
* @date April 08, 2025
*/
@ExtendWith(MockitoExtension.class)
class MembershipControllerTest {

    @Mock
    private MembershipService membershipService;

    @InjectMocks
    private MembershipController membershipController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private Membership membership;
    private MembershipRequest membershipRequest;
    private Promotion promotion;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(membershipController).build();
        objectMapper = new ObjectMapper();

        membership = new Membership();
        membership.setMbsId(1L);
        membership.setMbsName("Gold Membership");
        membership.setMbsDescription("Premium membership benefits");

        membershipRequest = new MembershipRequest();
        membershipRequest.setMbsName("Gold Membership");
        membershipRequest.setMbsDescription("Premium membership benefits");

        promotion = new Promotion();
        promotion.setPromId(1L);
        promotion.setPromName("10% Discount");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllMemberships_Success() {
        List<Membership> memberships = Arrays.asList(membership);
        PaginationWrapper<List<Membership>> paginationWrapper = new PaginationWrapper.Builder<List<Membership>>()
                .setData(memberships)
                .setPage(0)
                .setSize(10)
                .setTotalPages(1)
                .setTotalElements(memberships.size())
                .build();

        when(membershipService.getAllMemberships(0, 10)).thenReturn(paginationWrapper);

        ResponseEntity<ResponseObject<List<Membership>>> response = membershipController.getAllMemberships(0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
        assertEquals("Get customers list successfully!", response.getBody().getMessage());
        assertNotNull(response.getBody().getContent());
        assertFalse(response.getBody().getContent().isEmpty());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Gold Membership", response.getBody().getContent().get(0).getMbsName());

        verify(membershipService, times(1)).getAllMemberships(0, 10);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetMembershipById_Success() {
        Long id = 1L;
        when(membershipService.getMembershipById(id)).thenReturn(membership);

        ResponseEntity<ResponseObject<Membership>> response = membershipController.getMembershipById(id);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
        assertEquals("Get user by Id successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getContent());
        assertEquals("Gold Membership", response.getBody().getContent().getMbsName());

        verify(membershipService, times(1)).getMembershipById(id);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSearchMembershipByName_Success() {
        String mbsName = "Gold";
        List<Membership> memberships = Arrays.asList(membership);
        PaginationWrapper<List<Membership>> paginationWrapper = new PaginationWrapper.Builder<List<Membership>>()
                .setData(memberships)
                .setPage(0)
                .setSize(10)
                .setTotalPages(1)
                .setTotalElements(memberships.size())
                .build();

        when(membershipService.findMembershipByName(mbsName, 0, 10)).thenReturn(paginationWrapper);

        ResponseEntity<ResponseObject<List<Membership>>> response = membershipController.searchMembershipByName(mbsName,
                0, 10);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
        assertEquals("Get customers list successfully!", response.getBody().getMessage());
        assertNotNull(response.getBody().getContent());
        assertFalse(response.getBody().getContent().isEmpty());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals("Gold Membership", response.getBody().getContent().get(0).getMbsName());

        verify(membershipService, times(1)).findMembershipByName(mbsName, 0, 10);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateMembership_Success() {
        when(membershipService.createMembership(any(MembershipRequest.class))).thenReturn(membership);

        ResponseEntity<ResponseObject<Membership>> response = membershipController.createMembership(membershipRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.CREATED.value(), response.getBody().getCode());
        assertEquals("Create user successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getContent());
        assertEquals("Gold Membership", response.getBody().getContent().getMbsName());

        verify(membershipService, times(1)).createMembership(any(MembershipRequest.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateMembership_Success() {
        Long id = 1L;
        MembershipRequest request = new MembershipRequest();
        request.setMbsName("Gold Membership");
        request.setMbsDescription("Premium membership benefits");

        Membership updatedMembership = new Membership();
        updatedMembership.setMbsId(id);
        updatedMembership.setMbsName("Gold Membership");
        updatedMembership.setMbsDescription("Premium membership benefits");

        when(membershipService.updateMembership(eq(id), any(MembershipRequest.class))).thenReturn(updatedMembership);

        ResponseEntity<ResponseObject<Membership>> response = membershipController.updateMembership(id, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals(HttpStatus.OK.value(), response.getBody().getCode());
        assertEquals("Update user successfully", response.getBody().getMessage());
        assertEquals(updatedMembership, response.getBody().getContent());

        verify(membershipService, times(1)).updateMembership(eq(id), any(MembershipRequest.class));
    }

    @Test
    void testDeleteMembership_Success() throws Exception {
        doNothing().when(membershipService).deleteMembership(1L);

        mockMvc.perform(delete("/api/v1/memberships/1"))
                .andExpect(status().isOk());

        verify(membershipService, times(1)).deleteMembership(1L);
    }

    @Test
    void testAssignPromotionOnRankUp_Success() throws Exception {
        doNothing().when(membershipService).assignPromotionOnRankUp(1L, 1L, "user1");

        mockMvc.perform(post("/api/v1/memberships/assign-promotion")
                .param("userId", "1")
                .param("mbsId", "1")
                .param("username", "user1"))
                .andExpect(status().isOk());

        verify(membershipService, times(1)).assignPromotionOnRankUp(1L, 1L, "user1");
    }

    @Test
    void testApplyMembershipToUser_Success() throws Exception {
        doNothing().when(membershipService).applyMembershipToUser(1L, 1L);

        mockMvc.perform(post("/api/v1/memberships/apply-to-user")
                .param("userId", "1")
                .param("mbsId", "1"))
                .andExpect(status().isOk());

        verify(membershipService, times(1)).applyMembershipToUser(1L, 1L);
    }

    @Test
    void testAutoUpdateMembership_Success() throws Exception {
        doNothing().when(membershipService).autoUpdateMembership(1L, 1000L);

        mockMvc.perform(post("/api/v1/memberships/auto-update")
                .param("userId", "1")
                .param("accumulatedPoints", "1000"))
                .andExpect(status().isOk());

        verify(membershipService, times(1)).autoUpdateMembership(1L, 1000L);
    }
}