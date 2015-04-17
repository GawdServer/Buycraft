package net.buycraft.util;

public class PackageCommand implements Comparable<Object> {
    private final int id;
    public final String username;
    public final String command;
    public final long runtime;

    public final int requiredInventorySlots;

    public PackageCommand(int id, String username, String command, int tickDelay, int requiredInventorySlots)
    {
        this.id = id;
        this.username = username;
        this.command = command;
        this.runtime = System.currentTimeMillis() + tickDelay * 50L;

        this.requiredInventorySlots = requiredInventorySlots;
    }

    public int getId()
    {
        return id;
    }

    public boolean requiresFreeInventorySlots() {
        return requiredInventorySlots > 0;
    }

    public int compareTo(Object o) {
        // If the objects are the same return 0
        if (this == o)
            return 0;

        if (o.getClass() == Integer.class) {
            return compareTo((Integer) o);
        } else if (o instanceof PackageCommand) {
            return compareTo((PackageCommand) o);
        }

        // Just do something random
        return hashCode() > o.hashCode() ? 1: -1;
    }

    public int compareTo(PackageCommand o) {
        if (id == o.id)
            return 0;

        if (runtime > o.runtime)
            return 1;

        if (runtime < o.runtime)
            return -1;

        // Make sure the commands are ordered correctly
        return id > o.id ? 1 : -1;
    }

    public int compareTo(Integer i) {
        return id > i ? 1 : id == i ? 0 : -1;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PackageCommand other = (PackageCommand) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
