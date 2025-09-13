package model;

public class Process {
    private String name;
    private long originalTime;
    private long remainingTime;
    private Status status; 
    private int cycleCount;
    
   
    private Status suspendedReady;    // SUSPENDIDO_LISTO o NO_SUSPENDIDO_LISTO
    private Status suspendedBlocked;  // SUSPENDIDO_BLOQUEADO o NO_SUSPENDIDO_BLOQUEADO
    private Status resumed;           // REANUDADO o NO_REANUDADO

    public Process(String name, long time, Status status) {
        this.name = name;
        this.originalTime = time;
        this.remainingTime = time;
        this.status = status;
        this.cycleCount = 0;
        this.suspendedReady = Status.NO_SUSPENDIDO_LISTO;
        this.suspendedBlocked = Status.NO_SUSPENDIDO_BLOQUEADO;
        this.resumed = Status.NO_REANUDADO;
    }
    
    public Process(String name, long time, Status status, 
                   Status suspendedReady, Status suspendedBlocked, Status resumed) {
        this.name = name;
        this.originalTime = time;
        this.remainingTime = time;
        this.status = status;
        this.cycleCount = 0;
        this.suspendedReady = suspendedReady;
        this.suspendedBlocked = suspendedBlocked;
        this.resumed = resumed;
    }

    public Process(String name, long originalTime, long remainingTime, Status status, int cycleCount,
                   Status suspendedReady, Status suspendedBlocked, Status resumed) {
        this.name = name;
        this.originalTime = originalTime;
        this.remainingTime = remainingTime;
        this.status = status;
        this.cycleCount = cycleCount;
        this.suspendedReady = suspendedReady;
        this.suspendedBlocked = suspendedBlocked;
        this.resumed = resumed;
    }

    public void subtractTime(long time) {
        this.remainingTime -= time;
        if (remainingTime < 0) {
            remainingTime = 0;
        }
    }

    public void incrementCycle() {
        this.cycleCount++;
    }

    public boolean isFinished() {
        return remainingTime <= 0;
    }

    public boolean isBlocked() {
        return status == Status.BLOQUEADO;
    }

    public boolean isSuspendedReady() {
        return suspendedReady == Status.SUSPENDIDO_LISTO;
    }
    
    public boolean isSuspendedBlocked() {
        return suspendedBlocked == Status.SUSPENDIDO_BLOQUEADO;
    }

    public boolean isResumed() {
        return resumed == Status.REANUDADO;
    }

    public void resetTime() {
        remainingTime = originalTime;
    }


    public String getName() {
        return name;
    }

    public long getOriginalTime() {
        return originalTime;
    }

    public long getRemainingTime() {
        return remainingTime;
    }

    public Status getStatus() {
        return status;
    }

    public int getCycleCount() {
        return cycleCount;
    }

    public String getStatusString() {
        return status == Status.BLOQUEADO ? "Bloqueado" : "No Bloqueado";
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

  
    public void setName(String name) {
        this.name = name;
    }

    public void setOriginalTime(long originalTime) {
        this.originalTime = originalTime;
        this.remainingTime = originalTime;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setCycleCount(int cycleCount) {
        this.cycleCount = cycleCount;
    }

    public void setSuspendedReady(Status suspendedReady) {
        this.suspendedReady = suspendedReady;
    }
    
    public void setSuspendedBlocked(Status suspendedBlocked) {
        this.suspendedBlocked = suspendedBlocked;
    }

    public void setResumed(Status resumed) {
        this.resumed = resumed;
    }

    public Process clone() {
        return new Process(name, originalTime, remainingTime, status, cycleCount,
                          suspendedReady, suspendedBlocked, resumed);
    }

    @Override
    public String toString() {
        return "Process{" +
                "name='" + name + '\'' +
                ", originalTime=" + originalTime +
                ", remainingTime=" + remainingTime +
                ", status=" + status +
                ", cycleCount=" + cycleCount +
                ", suspendedReady=" + suspendedReady +
                ", suspendedBlocked=" + suspendedBlocked +
                ", resumed=" + resumed +
                '}';
    }
}