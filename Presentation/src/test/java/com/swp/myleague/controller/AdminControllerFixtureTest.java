package com.swp.myleague.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpSession;

import com.swp.myleague.model.entities.match.Match;
import com.swp.myleague.model.service.matchservice.MatchService;

class AdminControllerFixtureTest {

    @Mock
    MatchService matchService;

    @Mock
    Model model;

    @Mock
    HttpSession session;

    @InjectMocks
    AdminController adminController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private Match mk(String desc) {
        Match m = new Match();
        m.setMatchDescription(desc);
        return m;
    }

    @Test
    void TC1_CreateFixturesWhenSessionEmpty() {
        when(session.getAttribute("autoFixturesMatch")).thenReturn(null);
        List<Match> generated = List.of(mk("Vòng 1"), mk("Vòng 2"));
        when(matchService.autoGenFixturesMatches(any(LocalDate.class), anyList())).thenReturn(generated);

        String res = adminController.getAddFixtures(model, "2025-11-14", session, false);
        assertEquals("redirect:/admin", res);
        verify(matchService).autoGenFixturesMatches(any(LocalDate.class), anyList());
        verify(session).setAttribute(eq("autoFixturesMatch"), eq(generated));
        verify(model).addAttribute(eq("hasAutoFixtureSession"), eq(true));
    }

    @Test
    void TC2_UseFixturesFromSessionWhenNotRecreate() {
        List<Match> fixtures = List.of(mk("Vòng 1"));
        when(session.getAttribute("autoFixturesMatch")).thenReturn(fixtures);

        String res = adminController.getAddFixtures(model, "2025-11-14", session, false);
        assertEquals("redirect:/admin", res);
        verify(matchService, never()).autoGenFixturesMatches(any(), anyList());
        verify(model).addAttribute(eq("fixtures"), eq(fixtures));
        verify(model).addAttribute(eq("fixturesByRound"), anyMap());
    }

    @Test
    void TC3_RecreateForcesRegeneration() {
        List<Match> fixtures = List.of(mk("Vòng 1"));
        when(session.getAttribute("autoFixturesMatch")).thenReturn(fixtures);
        List<Match> regenerated = List.of(mk("Vòng 1"), mk("Vòng 2"));
        when(matchService.autoGenFixturesMatches(any(LocalDate.class), anyList())).thenReturn(regenerated);

        String res = adminController.getAddFixtures(model, "2025-11-14", session, true);
        assertEquals("redirect:/admin", res);
        verify(matchService).autoGenFixturesMatches(any(LocalDate.class), anyList());
        verify(session).setAttribute(eq("autoFixturesMatch"), eq(regenerated));
    }

    @Test
    void TC4_FixturesGroupedByRoundCorrectly() {
        List<Match> generated = List.of(mk("Vòng 2"), mk("Vòng 1"), mk("Vòng 1"));
        when(session.getAttribute("autoFixturesMatch")).thenReturn(null);
        when(matchService.autoGenFixturesMatches(any(LocalDate.class), anyList())).thenReturn(generated);

        adminController.getAddFixtures(model, "2025-11-14", session, false);

        ArgumentCaptor<Map> capt = ArgumentCaptor.forClass(Map.class);
        verify(model).addAttribute(eq("fixturesByRound"), capt.capture());
        Map<?, ?> map = capt.getValue();
        assertEquals(2, ((List<?>) map.get(1)).size());
        assertEquals(1, ((List<?>) map.get(2)).size());
    }

    @Test
    void TC5_SaveFixturesForOneRound() {
        List<Match> fixtures = List.of(mk("Vòng 1"), mk("Vòng 2"), mk("Vòng 1"));
        when(session.getAttribute("autoFixturesMatch")).thenReturn(fixtures);

        String res = adminController.postAddFixtures(1, session);
        assertEquals("redirect:/admin", res);
        ArgumentCaptor<List> capt = ArgumentCaptor.forClass(List.class);
        verify(matchService).saveAuto(capt.capture());
        List<Match> saved = capt.getValue();
        assertTrue(saved.stream().allMatch(m -> m.getMatchDescription().contains("1")));
    }

    @Test
    void TC6_DoNotSaveWhenNoSessionFixtures() {
        when(session.getAttribute("autoFixturesMatch")).thenReturn(null);
        String res = adminController.postAddFixtures(1, session);
        assertEquals("redirect:/admin", res);
        verify(matchService, never()).saveAuto(anyList());
    }

    @Test
    void TC7_SaveFixturesForLastRound() {
        List<Match> fixtures = List.of(mk("Vòng 1"), mk("Vòng 3"), mk("Vòng 2"));
        when(session.getAttribute("autoFixturesMatch")).thenReturn(fixtures);

        String res = adminController.postAddFixtures(3, session);
        assertEquals("redirect:/admin", res);
        ArgumentCaptor<List> capt = ArgumentCaptor.forClass(List.class);
        verify(matchService).saveAuto(capt.capture());
        List<Match> saved = capt.getValue();
        assertTrue(saved.stream().allMatch(m -> m.getMatchDescription().contains("3")));
    }

    @Test
    void TC8_ParseStartDateFormat_Valid() {
        when(session.getAttribute("autoFixturesMatch")).thenReturn(null);
        when(matchService.autoGenFixturesMatches(any(LocalDate.class), anyList())).thenReturn(new ArrayList<>());

        assertDoesNotThrow(() -> adminController.getAddFixtures(model, "2025-11-14", session, false));
        verify(matchService).autoGenFixturesMatches(any(LocalDate.class), anyList());
    }

    @Test
    void TC9_HandleNoFixturesCreated() {
        when(session.getAttribute("autoFixturesMatch")).thenReturn(null);
        when(matchService.autoGenFixturesMatches(any(LocalDate.class), anyList())).thenReturn(new ArrayList<>());

        adminController.getAddFixtures(model, "2025-11-14", session, false);
        verify(model).addAttribute(eq("fixtures"), eq(new ArrayList<>()));
        ArgumentCaptor<Map> capt = ArgumentCaptor.forClass(Map.class);
        verify(model).addAttribute(eq("fixturesByRound"), capt.capture());
        Map<?, ?> map = capt.getValue();
        assertTrue(map.isEmpty());
    }

    @Test
    void TC10_FixturesByRoundSorted() {
        List<Match> generated = List.of(mk("Vòng 10"), mk("Vòng 2"), mk("Vòng 1"));
        when(session.getAttribute("autoFixturesMatch")).thenReturn(null);
        when(matchService.autoGenFixturesMatches(any(LocalDate.class), anyList())).thenReturn(generated);

        adminController.getAddFixtures(model, "2025-11-14", session, false);
        ArgumentCaptor<Map> capt = ArgumentCaptor.forClass(Map.class);
        verify(model).addAttribute(eq("fixturesByRound"), capt.capture());
        Map<?, ?> map = capt.getValue();
        List<?> keys = new ArrayList<>(map.keySet());
        assertEquals(List.of(1, 2, 10), keys);
    }
}
