package me.genn.thegrandtourney.player;

public class BankTransaction {
    public float amount;
    public long time;
    public String cause;

    public BankTransaction(float amount, long time, String cause) {
        this.amount = amount;
        this.time = time;
        this.cause = cause;
    }
}
