package com.freeroom.persistence.beans;

import com.freeroom.persistence.annotations.ID;
import com.freeroom.persistence.annotations.Persist;

public class Order
{
    @ID
    private long orderid;

    @Persist
    private int amount;

    @Persist
    private String memo;

    public int getAmount()
    {
        return amount;
    }

    public void setAmount(final int amount)
    {
        this.amount = amount;
    }

    public String getMemo()
    {
        return memo;
    }

    public void setMemo(final String memo)
    {
        this.memo = memo;
    }
}
