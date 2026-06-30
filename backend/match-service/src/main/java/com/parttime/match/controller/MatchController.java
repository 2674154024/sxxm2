package com.parttime.match.controller;

import com.parttime.common.response.R;
import com.parttime.match.dto.request.MatchCalculateRequest;
import com.parttime.match.dto.request.MatchCandidateRequest;
import com.parttime.match.dto.request.MatchRecommendRequest;
import com.parttime.match.dto.response.MatchCandidateResponse;
import com.parttime.match.dto.response.MatchRecommendResponse;
import com.parttime.match.dto.response.MatchScoreResponse;
import com.parttime.match.service.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    @PostMapping("/match/calculate")
    public R<MatchScoreResponse> calculate(@Validated @RequestBody MatchCalculateRequest request) {
        return R.ok(matchService.calculate(request));
    }

    @PostMapping("/match/recommend")
    public R<List<MatchRecommendResponse>> recommend(@Validated @RequestBody MatchRecommendRequest request) {
        return R.ok(matchService.recommend(request));
    }

    @PostMapping("/enterprise/match-candidates")
    public R<List<MatchCandidateResponse>> matchCandidates(@Validated @RequestBody MatchCandidateRequest request) {
        return R.ok(matchService.matchCandidates(request));
    }
}