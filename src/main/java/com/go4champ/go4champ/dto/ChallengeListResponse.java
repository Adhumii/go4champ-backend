package com.go4champ.go4champ.dto;

import java.util.List;

/**
 * DTO für Challenge Listen (mit Pagination Support)
 */
public class ChallengeListResponse {

    private List<ChallengeResponse> challenges;
    private long totalCount;
    private int page;
    private int size;
    private boolean hasNext;
    private boolean hasPrevious;

    // Constructors
    public ChallengeListResponse() {}

    public ChallengeListResponse(List<ChallengeResponse> challenges, long totalCount, int page,
                                 int size, boolean hasNext, boolean hasPrevious) {
        this.challenges = challenges;
        this.totalCount = totalCount;
        this.page = page;
        this.size = size;
        this.hasNext = hasNext;
        this.hasPrevious = hasPrevious;
    }

    // Getters and Setters
    public List<ChallengeResponse> getChallenges() { return challenges; }
    public void setChallenges(List<ChallengeResponse> challenges) { this.challenges = challenges; }

    public long getTotalCount() { return totalCount; }
    public void setTotalCount(long totalCount) { this.totalCount = totalCount; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }

    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }

    public boolean isHasPrevious() { return hasPrevious; }
    public void setHasPrevious(boolean hasPrevious) { this.hasPrevious = hasPrevious; }
}