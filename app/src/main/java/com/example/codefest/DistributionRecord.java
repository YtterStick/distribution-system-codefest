package com.example.codefest;

public class DistributionRecord {
    private final int id;
    private final String householdName;
    private final int familySize;
    private final int riceDistributed;
    private final int cansDistributed;
    private final boolean isPriority;
    private final String timestamp;

    public DistributionRecord(int id, String householdName, int familySize, int riceDistributed, int cansDistributed, boolean isPriority, String timestamp) {
        this.id = id;
        this.householdName = householdName;
        this.familySize = familySize;
        this.riceDistributed = riceDistributed;
        this.cansDistributed = cansDistributed;
        this.isPriority = isPriority;
        this.timestamp = timestamp;
    }

    // Getters
    public int getId() { return id; }
    public String getHouseholdName() { return householdName; }
    public int getFamilySize() { return familySize; }
    public int getRiceDistributed() { return riceDistributed; }
    public int getCansDistributed() { return cansDistributed; }
    public boolean isPriority() { return isPriority; }
    public String getTimestamp() { return timestamp; }
}
