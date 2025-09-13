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

    // Método básico para agregar proceso
    public void addProcess(String name, long time, Status status) {
        Process process = new Process(name, time, status);
        initialProcesses.add(process);
    }

    // Método completo para agregar proceso con estados de suspensión
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

    // Método básico para editar proceso
    public void editProcess(int position, String processName, long newTime, Status newStatus) {
        if (position >= 0 && position < initialProcesses.size()) {
            Process existingProcess = initialProcesses.get(position);
            if (existingProcess.getName().equalsIgnoreCase(processName)) {
                Process updatedProcess = new Process(processName, newTime, newStatus);
                initialProcesses.set(position, updatedProcess);
            }
        }
    }

    // Método completo para editar proceso con estados de suspensión
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
        
        // Clonar procesos para la simulación
        ArrayList<Process> processQueue = cloneProcesses();
        
        // Ejecutar simulación FIFO
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
        // Estado: Listo
        addLog(process, Filter.LISTO);

        // Estado: Despachado
        addLog(process, Filter.DESPACHADO);

        // Estado: En Ejecución
        process.subtractTime(Constants.QUANTUM_TIME);
        process.incrementCycle();
        addLog(process, Filter.EN_EJECUCION);

        // Verificar si el proceso terminó
        if (process.isFinished()) {
            addLog(process, Filter.FINALIZADO);
            return;
        }

        // Determinar el siguiente estado según el diagrama
        if (process.isBlocked()) {
            handleBlockedProcess(process, queue);
        } else if (process.isSuspendedReady()) {
            handleSuspendedReadyProcess(process, queue);
        } else {
            // Proceso normal: Tiempo Expirado -> vuelve a Listo
            addLog(process, Filter.TIEMPO_EXPIRADO);
            queue.add(process);
        }
    }

    private void handleBlockedProcess(Process process, ArrayList<Process> queue) {
        // Estado: Bloqueado
        addLog(process, Filter.BLOQUEADO);
        
        if (process.isSuspendedBlocked()) {
            // Ruta: Suspender -> Suspendido Bloqueado
            addLog(process, Filter.SUSPENDER_BLOQUEADOS);
            addLog(process, Filter.SUSPENDIDO_BLOQUEADO);
            
            // CASO ESPECIAL: Si también tiene suspensión de listo marcada
            if (process.isSuspendedReady()) {
                // NUEVA TRANSICIÓN: Suspendido Bloqueado → Suspendido Listo
                addLog(process, Filter.TRANSICION_BLOQUEADO_A_LISTO);
                
                // Transición especial: Suspendido Bloqueado → Suspendido Listo
                addLog(process, Filter.SUSPENDIDO_LISTO);
                
                // Si está marcado como reanudado, continúa el flujo
                if (process.isResumed()) {
                    // Reanudar desde Suspendido Listo
                    addLog(process, Filter.REANUDAR_LISTOS);
                    
                    // VUELVE DIRECTAMENTE A LA COLA (estado Listo en el próximo ciclo)
                    queue.add(process);
                } else {
                    // Se queda en Suspendido Listo (no se reanuda aún)
                    // El proceso no vuelve a la cola hasta ser reanudado
                }
                return;
            } else {
                // Flujo normal: solo suspensión bloqueada
                if (process.isResumed()) {
                    addLog(process, Filter.REANUDAR_BLOQUEADOS);
                    
                    // Vuelve al estado Bloqueado después de reanudar
                    addLog(process, Filter.BLOQUEADO);
                    
                    // Estado: Despertar (Terminación de E/S)
                    addLog(process, Filter.DESPERTAR);
                } else {
                    // Se queda en Suspendido Bloqueado (no se reanuda aún)
                    return;
                }
            }
        } else {
            // Proceso bloqueado normal (sin suspensión bloqueada)
            // Estado: Despertar (Terminación de E/S)
            addLog(process, Filter.DESPERTAR);
        }
        
        // Vuelve a la cola para el próximo ciclo
        queue.add(process);
    }

    private void handleSuspendedReadyProcess(Process process, ArrayList<Process> queue) {
        // Ruta: Suspender -> Suspendido Listo
        addLog(process, Filter.SUSPENDER_LISTOS);
        addLog(process, Filter.SUSPENDIDO_LISTO);
        
        // Solo continúa si está marcado como reanudado
        if (process.isResumed()) {
            addLog(process, Filter.REANUDAR_LISTOS);
            
            // Después de reanudar, vuelve a la cola
            queue.add(process);
        } else {
            // Se queda en Suspendido Listo (no se reanuda aún)
            // El proceso no vuelve a la cola hasta ser reanudado
        }
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

    // Métodos para obtener procesos según sus estados de suspensión
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

    // Métodos para obtener logs de estados específicos de suspensión
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
        
        // Buscar logs de ambos tipos de reanudación
        List<Log> resumedReadyLogs = getLogsByFilter(Filter.REANUDAR_LISTOS);
        List<Log> resumedBlockedLogs = getLogsByFilter(Filter.REANUDAR_BLOQUEADOS);
        
        // Procesar logs de reanudación de listos
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
        
        // Procesar logs de reanudación de bloqueados
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