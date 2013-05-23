package com.freeroom.persistence.beans;

import com.freeroom.persistence.annotations.ID;
import com.freeroom.persistence.annotations.Persist;
import com.freeroom.persistence.proxy.IdPurpose;

public class Order
{
    @ID
    private long orderid;

    @Persist
    private int amount;

    @Persist
    private String memo;

    private IdPurpose idPurpose;

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

    public void setIdPurpose(final IdPurpose idPurpose)
    {
        this.idPurpose = idPurpose;
    }

    public void setOrderid(final long orderid)
    {
        this.orderid = orderid;
    }
}
