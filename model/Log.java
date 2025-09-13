package model;

public class Log {
    private String processName;
    private long remainingTime;
    private Status status;
    private Filter filter;
    private int cycleCount;
    private long timestamp;
    
    // Estados de suspensión
    private Status suspendedReady;
    private Status suspendedBlocked;
    private Status resumed;

    public Log(Process process, Filter filter) {
        this.processName = process.getName();
        this.remainingTime = process.getRemainingTime();
        this.status = process.getStatus();
        this.cycleCount = process.getCycleCount();
        this.filter = filter;
        this.timestamp = System.currentTimeMillis();
        this.suspendedReady = process.getSuspendedReady();
        this.suspendedBlocked = process.getSuspendedBlocked();
        this.resumed = process.getResumed();
    }

    public String getProcessName() {
        return processName;
    }

    public long getRemainingTime() {
        return remainingTime;
    }

    public Status getStatus() {
        return status;
    }

    public String getStatusString() {
        return status == Status.BLOQUEADO ? "Bloqueado" : "No Bloqueado";
    }

    public Filter getFilter() {
        return filter;
    }

    public int getCycleCount() {
        return cycleCount;
    }

    public long getTimestamp() {
        return timestamp;
    }
    
    public Status getSuspendedReady() {
        return suspendedReady;
    }
    
    public Status getSuspendedBlocked() {
        return suspendedBlocked;
    }
    
    public Status getResumed() {
        return resumed;
    }
    
    public String getSuspendedReadyString() {
        return suspendedReady == Status.SUSPENDIDO_LISTO ? "Si" : "No";
    }
    
    public String getSuspendedBlockedString() {
        return suspendedBlocked == Status.SUSPENDIDO_BLOQUEADO ? "Si" : "No";
    }
    
    public String getResumedString() {
        return resumed == Status.REANUDADO ? "Si" : "No";
    }

    @Override
    public String toString() {
        return "Log{" +
                "processName='" + processName + '\'' +
                ", remainingTime=" + remainingTime +
                ", status=" + status +
                ", filter=" + filter +
                ", cycleCount=" + cycleCount +
                ", suspendedReady=" + suspendedReady +
                ", suspendedBlocked=" + suspendedBlocked +
                ", resumed=" + resumed +
                '}';
    }
}