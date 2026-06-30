package com.parttime.match.service;

import com.parttime.match.dto.request.MatchCalculateRequest;
import com.parttime.match.dto.request.MatchCandidateRequest;
import com.parttime.match.dto.request.MatchRecommendRequest;
import com.parttime.match.dto.response.MatchCandidateResponse;
import com.parttime.match.dto.response.MatchRecommendResponse;
import com.parttime.match.dto.response.MatchScoreResponse;

import java.util.List;

public interface MatchService {

    MatchScoreResponse calculate(MatchCalculateRequest request);

    List<MatchRecommendResponse> recommend(MatchRecommendRequest request);

    List<MatchCandidateResponse> matchCandidates(MatchCandidateRequest request);
}