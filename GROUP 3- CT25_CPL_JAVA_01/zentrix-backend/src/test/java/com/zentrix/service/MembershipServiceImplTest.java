package com.zentrix.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.zentrix.model.entity.Membership;
import com.zentrix.model.exception.ActionFailedException;
import com.zentrix.model.exception.ValidationFailedException;
import com.zentrix.model.exception.AppCode;
import com.zentrix.model.request.MembershipRequest;
import com.zentrix.model.response.PaginationWrapper;
import com.zentrix.repository.MembershipRepository;

/*
 * @author Nguyen Le Khac Vu - CE180175 - CT25_CPL_JAVA_01
 * @date March 13, 2025
 */
@ExtendWith(MockitoExtension.class)
public class MembershipServiceImplTest {

    @Mock
    private MembershipRepository membershipRepository;

    @InjectMocks
    private MembershipServiceImpl membershipService;

    private Membership membership;
    private MembershipRequest membershipRequest;

    @BeforeEach
    void setUp() {
        membership = new Membership();
        membership.setMbsId(1L);
        membership.setMbsName("Gold Membership");
        membership.setMbsDescription("Premium membership benefits");
        membership.setMbsPoint(1l);

        membershipRequest = new MembershipRequest();
        membershipRequest.setMbsName("Gold Membership");
        membershipRequest.setMbsDescription("Premium membership benefits");
        membershipRequest.setMbsPoint(1l);
    }

    @Test
    void testGetAllMemberships_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Membership> page = new PageImpl<>(List.of(membership), pageable, 1);
        when(membershipRepository.findAll(pageable)).thenReturn(page);

        PaginationWrapper<List<Membership>> result = membershipService.getAllMemberships(0, 10);

        assertNotNull(result);
        assertFalse(result.getData().isEmpty());
        assertEquals(1, result.getData().size());
        verify(membershipRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetAllMemberships_Failure() {
        Pageable pageable = PageRequest.of(0, 10);
        when(membershipRepository.findAll(pageable)).thenThrow(new RuntimeException("Database error"));

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> membershipService.getAllMemberships(0, 10));
        assertEquals(AppCode.USER_GET_LIST_FAILED.getCode(), exception.getErrors().getCode());
    }

    @Test
    void testGetMembershipById_Success() {
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership));

        Membership result = membershipService.getMembershipById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getMbsId());
        verify(membershipRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateMembership_Success() {
        when(membershipRepository.save(any(Membership.class))).thenReturn(membership);

        Membership result = membershipService.createMembership(membershipRequest);

        assertNotNull(result);
        assertEquals("Gold Membership", result.getMbsName());
        verify(membershipRepository, times(1)).save(any(Membership.class));
    }

    @Test
    void testCreateMembership_InvalidRequest() {
        membershipRequest.setMbsName(null);

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> membershipService.createMembership(membershipRequest));
        assertEquals(AppCode.MEMBERSHIP_CREATION_FAILED.getCode(), exception.getErrors().getCode());
    }

    @Test
    void testSaveMembership_Success() {
        when(membershipRepository.save(membership)).thenReturn(membership);

        Membership result = membershipService.saveMembership(membership);

        assertNotNull(result);
        assertEquals("Gold Membership", result.getMbsName());
        verify(membershipRepository, times(1)).save(membership);
    }

    @Test
    void testSaveMembership_NullInput() {
        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> membershipService.saveMembership(null));
        assertEquals(AppCode.MEMBERSHIP_UPDATE_FAILED.getCode(), exception.getErrors().getCode());
    }

    @Test
    void testDeleteMembership_Success() {
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership));
        doNothing().when(membershipRepository).deleteById(1L);

        membershipService.deleteMembership(1L);

        verify(membershipRepository, times(1)).findById(1L);
        verify(membershipRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteMembership_NotFound() {
        when(membershipRepository.findById(1L)).thenReturn(Optional.empty());

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> membershipService.deleteMembership(1L));
        assertEquals(AppCode.MEMBERSHIP_DELETE_FAILED.getCode(), exception.getErrors().getCode());
    }

    @Test
    void testFindMembershipByName_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Membership> page = new PageImpl<>(List.of(membership), pageable, 1);
        when(membershipRepository.findByMbsName("Gold Membership", pageable)).thenReturn(page);

        PaginationWrapper<List<Membership>> result = membershipService.findMembershipByName("Gold Membership", 0, 10);

        assertNotNull(result);
        assertFalse(result.getData().isEmpty());
        assertEquals(1, result.getData().size());
        verify(membershipRepository, times(1)).findByMbsName("Gold Membership", pageable);
    }

    @Test
    void testFindMembershipByName_NotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Membership> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(membershipRepository.findByMbsName("Silver Membership", pageable)).thenReturn(emptyPage);

        ValidationFailedException exception = assertThrows(ValidationFailedException.class,
                () -> membershipService.findMembershipByName("Silver Membership", 0, 10));
        assertEquals(AppCode.USER_NOT_FOUND.getCode(), exception.getErrors().getCode());
    }

    @Test
    void testUpdateMembership_Success() {
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership));
        when(membershipRepository.save(any(Membership.class))).thenReturn(membership);

        Membership updated = membershipService.updateMembership(1L, membershipRequest);

        assertNotNull(updated);
        assertEquals("Gold Membership", updated.getMbsName());
        verify(membershipRepository, times(1)).findById(1L);
        verify(membershipRepository, times(1)).save(any(Membership.class));
    }

    @Test
    void testUpdateMembership_NotFound() {
        when(membershipRepository.findById(1L)).thenReturn(Optional.empty());

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> membershipService.updateMembership(1L, membershipRequest));
        assertEquals(AppCode.MEMBERSHIP_NOT_FOUND.getCode(), exception.getErrors().getCode());
    }

    @Test
    void testUpdateMembership_InvalidRequest() {
        membershipRequest.setMbsName(null);

        ActionFailedException exception = assertThrows(ActionFailedException.class,
                () -> membershipService.updateMembership(1L, membershipRequest));
        assertEquals(AppCode.MEMBERSHIP_UPDATE_FAILED.getCode(), exception.getErrors().getCode());
    }
}