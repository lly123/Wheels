package com.freeroom.util;

public class Pair<A, B>
{
    public final A fst;
    public final B snd;

    public static <A, B> Pair<A, B> of(final A fst, final B snd)
    {
        return new Pair(fst, snd);
    }

    private Pair(A fst, B snd)
    {
        this.fst = fst;
        this.snd = snd;
    }

    private static boolean equals(final Object x, final Object y)
    {
        return (x == null && y == null) || (x != null && x.equals(y));
    }

    public boolean equals(final Object other)
    {
        return other instanceof Pair<?,?> &&
            equals(fst, ((Pair<?,?>)other).fst) &&
            equals(snd, ((Pair<?,?>)other).snd);
    }

    public int hashCode()
    {
        if (fst == null) {
            return (snd == null) ? 0 : snd.hashCode() + 1;
        } else if (snd == null) {
            return fst.hashCode() + 2;
        } else {
            return fst.hashCode() * 17 + snd.hashCode();
        }
    }

    public String toString()
    {
        return "Pair[" + fst + "," + snd + "]";
    }
}
