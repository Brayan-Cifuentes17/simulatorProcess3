package model;

import java.util.ArrayList;
import java.util.List;

public class ProcessManager {
    private ArrayList<Process> initialProcesses;
    private ArrayList<Log> executionLogs;

    public ProcessManager() {
        initialProcesses = new ArrayList<>();
        executionLogs = new ArrayList<>();
    }

  
    public void addProcess(String name, long time, Status status) {
        Process process = new Process(name, time, status);
        initialProcesses.add(process);
    }

   
    public void addProcess(String name, long time, Status status, 
                          Status suspendedReady, Status suspendedBlocked, Status resumed) {
        Process process = new Process(name, time, status, suspendedReady, suspendedBlocked, resumed);
        initialProcesses.add(process);
    }

    public boolean processExists(String name) {
        return initialProcesses.stream()
                .anyMatch(p -> p.getName().equalsIgnoreCase(name.trim()));
    }

    public void removeProcess(String name) {
        initialProcesses.removeIf(p -> p.getName().equalsIgnoreCase(name.trim()));
    }

   
    public void editProcess(int position, String processName, long newTime, Status newStatus) {
        if (position >= 0 && position < initialProcesses.size()) {
            Process existingProcess = initialProcesses.get(position);
            if (existingProcess.getName().equalsIgnoreCase(processName)) {
                Process updatedProcess = new Process(processName, newTime, newStatus);
                initialProcesses.set(position, updatedProcess);
            }
        }
    }

    
    public void editProcess(int position, String processName, long newTime, Status newStatus, 
                           Status suspendedReady, Status suspendedBlocked, Status resumed) {
        if (position >= 0 && position < initialProcesses.size()) {
            Process existingProcess = initialProcesses.get(position);
            if (existingProcess.getName().equalsIgnoreCase(processName)) {
                Process updatedProcess = new Process(processName, newTime, newTime, newStatus, 0,
                                                   suspendedReady, suspendedBlocked, resumed);
                initialProcesses.set(position, updatedProcess);
            }
        }
    }

    public boolean isEmpty() {
        return initialProcesses.isEmpty();
    }

    public void runSimulation() {
        executionLogs.clear();
        
       
        ArrayList<Process> processQueue = cloneProcesses();
        
      
        while (!processQueue.isEmpty()) {
            Process currentProcess = processQueue.remove(0);
            executeProcessCycle(currentProcess, processQueue);
        }
    }

    private ArrayList<Process> cloneProcesses() {
        ArrayList<Process> clones = new ArrayList<>();
        for (Process p : initialProcesses) {
            clones.add(p.clone());
        }
        return clones;
    }

    private void executeProcessCycle(Process process, ArrayList<Process> queue) {
        
        addLog(process, Filter.LISTO);

       
        addLog(process, Filter.DESPACHADO);

       
        process.subtractTime(Constants.QUANTUM_TIME);
        process.incrementCycle();
        addLog(process, Filter.EN_EJECUCION);

       
        if (process.isFinished()) {
            addLog(process, Filter.FINALIZADO);
            return;
        }

      
        if (process.isBlocked()) {
            handleBlockedProcess(process, queue);
        } else if (process.isSuspendedReady()) {
            handleSuspendedReadyProcess(process, queue);
        } else {
           
            addLog(process, Filter.TIEMPO_EXPIRADO);
            queue.add(process);
        }
    }

    private void handleBlockedProcess(Process process, ArrayList<Process> queue) {
    // Registrar que el proceso está bloqueado
        addLog(process, Filter.BLOQUEAR);
        addLog(process, Filter.BLOQUEADO);
        
        // Caso 1: Proceso bloqueado y suspendido bloqueado
        if (process.isSuspendedBlocked()) {
            // Suspender procesos bloqueados
            addLog(process, Filter.SUSPENDER_BLOQUEADOS);
            addLog(process, Filter.SUSPENDIDO_BLOQUEADO);
            
            // Si también está marcado como suspendido listo, hacer la transición especial
            if (process.isSuspendedReady()) {
                // Transición especial: Suspendido Bloqueado → Suspendido Listo
                addLog(process, Filter.TRANSICION_BLOQUEADO_A_LISTO);
                addLog(process, Filter.SUSPENDIDO_LISTO);
                addLog(process, Filter.REANUDAR_LISTOS);
                queue.add(process);

            } else {
                // Solo suspendido bloqueado, sin transición a suspendido listo
                    addLog(process, Filter.REANUDAR_BLOQUEADOS);
                    // Después de reanudar, sigue como bloqueado y luego despierta
                    addLog(process, Filter.BLOQUEADO);
                    addLog(process, Filter.DESPERTAR);
                    queue.add(process);
                
            }
        }
        // Caso 2: Proceso bloqueado y suspendido listo (pero NO suspendido bloqueado)
        else if (process.isSuspendedReady()) {
            // Primero despierta del bloqueo
            addLog(process, Filter.DESPERTAR);
            addLog(process, Filter.LISTO);
            // Luego se suspende como listo
            addLog(process, Filter.DE_LISTO_A_SUSPENDIDO);
            addLog(process, Filter.SUSPENDIDO_LISTO);
            
            addLog(process, Filter.REANUDAR_LISTOS);
               
            queue.add(process);
            
        }
        // Caso 3: Proceso bloqueado normal (sin suspensiones)
        else {
            // Simplemente despierta y vuelve a la cola
            addLog(process, Filter.DESPERTAR);
            queue.add(process);
        }
    }

    private void handleSuspendedReadyProcess(Process process, ArrayList<Process> queue) {
        
        addLog(process, Filter.SUSPENDER_LISTOS);
        addLog(process, Filter.SUSPENDIDO_LISTO);
        
        addLog(process, Filter.REANUDAR_LISTOS);
         queue.add(process);
    }

    private void addLog(Process process, Filter filter) {
        Log log = new Log(process, filter);
        executionLogs.add(log);
    }

    public List<Log> getLogsByFilter(Filter filter) {
        if (filter == Filter.TODO) {
            return new ArrayList<>(executionLogs);
        }
        
        return executionLogs.stream()
                .filter(log -> log.getFilter() == filter)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    
    public List<Process> getSuspendedReadyProcesses() {
        return initialProcesses.stream()
                .filter(Process::isSuspendedReady)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public List<Process> getSuspendedBlockedProcesses() {
        return initialProcesses.stream()
                .filter(Process::isSuspendedBlocked)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    public List<Process> getResumedProcesses() {
        return initialProcesses.stream()
                .filter(Process::isResumed)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }

    
    public List<Process> getSuspendedReadyFromLogs() {
        List<Process> suspendedProcesses = new ArrayList<>();
        List<Log> suspendedLogs = getLogsByFilter(Filter.SUSPENDIDO_LISTO);
        
        for (Log log : suspendedLogs) {
            Process originalProcess = findProcessByName(log.getProcessName());
            if (originalProcess != null) {
                Process suspendedProcess = new Process(
                    log.getProcessName(),
                    originalProcess.getOriginalTime(),
                    log.getRemainingTime(),
                    log.getStatus(),
                    log.getCycleCount(),
                    log.getSuspendedReady(),
                    log.getSuspendedBlocked(),
                    log.getResumed()
                );
                suspendedProcesses.add(suspendedProcess);
            }
        }
        
        return suspendedProcesses;
    }

    public List<Process> getSuspendedBlockedFromLogs() {
        List<Process> suspendedProcesses = new ArrayList<>();
        List<Log> suspendedLogs = getLogsByFilter(Filter.SUSPENDIDO_BLOQUEADO);
        
        for (Log log : suspendedLogs) {
            Process originalProcess = findProcessByName(log.getProcessName());
            if (originalProcess != null) {
                Process suspendedProcess = new Process(
                    log.getProcessName(),
                    originalProcess.getOriginalTime(),
                    log.getRemainingTime(),
                    log.getStatus(),
                    log.getCycleCount(),
                    log.getSuspendedReady(),
                    log.getSuspendedBlocked(),
                    log.getResumed()
                );
                suspendedProcesses.add(suspendedProcess);
            }
        }
        
        return suspendedProcesses;
    }

    public List<Process> getResumedFromLogs() {
        List<Process> resumedProcesses = new ArrayList<>();
        
        
        List<Log> resumedReadyLogs = getLogsByFilter(Filter.REANUDAR_LISTOS);
        List<Log> resumedBlockedLogs = getLogsByFilter(Filter.REANUDAR_BLOQUEADOS);
        
        
        for (Log log : resumedReadyLogs) {
            Process originalProcess = findProcessByName(log.getProcessName());
            if (originalProcess != null) {
                Process resumedProcess = new Process(
                    log.getProcessName(),
                    originalProcess.getOriginalTime(),
                    log.getRemainingTime(),
                    log.getStatus(),
                    log.getCycleCount(),
                    log.getSuspendedReady(),
                    log.getSuspendedBlocked(),
                    log.getResumed()
                );
                resumedProcesses.add(resumedProcess);
            }
        }
        
       
        for (Log log : resumedBlockedLogs) {
            Process originalProcess = findProcessByName(log.getProcessName());
            if (originalProcess != null) {
                Process resumedProcess = new Process(
                    log.getProcessName(),
                    originalProcess.getOriginalTime(),
                    log.getRemainingTime(),
                    log.getStatus(),
                    log.getCycleCount(),
                    log.getSuspendedReady(),
                    log.getSuspendedBlocked(),
                    log.getResumed()
                );
                resumedProcesses.add(resumedProcess);
            }
        }
        
        return resumedProcesses;
    }

    private Process findProcessByName(String name) {
        for (Process p : initialProcesses) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }

    public ArrayList<Process> getInitialProcesses() {
        return new ArrayList<>(initialProcesses);
    }

    public ArrayList<Log> getAllLogs() {
        return new ArrayList<>(executionLogs);
    }

    public void clearAll() {
        initialProcesses.clear();
        executionLogs.clear();
    }

    public void clearLogs() {
        executionLogs.clear();
    }
}