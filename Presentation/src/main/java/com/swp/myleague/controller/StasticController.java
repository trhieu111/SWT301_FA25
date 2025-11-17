package com.swp.myleague.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.swp.myleague.model.repo.MatchPlayerStatRepo;
import com.swp.myleague.model.service.informationservice.ClubService;
import com.swp.myleague.model.service.informationservice.PlayerService;
import com.swp.myleague.model.service.matchservice.MatchClubStatService;

@Controller
@RequestMapping(value = {"/stastics", "/stastics/"})
public class StasticController {

    @Autowired MatchClubStatService matchClubStatService;

    @Autowired MatchPlayerStatRepo matchPlayerStatRepo;

    @Autowired PlayerService playerService;

    @Autowired ClubService clubService;
    
    @GetMapping("")
    public String getStastics(@RequestParam(name = "year") String year , Model model) {
        model.addAttribute("rankingPlayerGoal", playerService.getTop10PlayerGoalByYear(year));
        model.addAttribute("rankingPlayerAssist", playerService.getTop10PlayerAssistByYear(year));
        model.addAttribute("rankingPlayerCleanSheet", playerService.getTop10PlayerCleanSheetByYear(year));
        model.addAttribute("rankingPlayerMinutedPlayed", playerService.getTop10PlayerMinutePlayedByYear(year));
        return "Stastics";
    }
    

}
