package model;

public enum Status {
    // Estados básicos de bloqueo
    BLOQUEADO,
    NO_BLOQUEADO,
    
    // Estados de suspensión listo
    SUSPENDIDO_LISTO,
    NO_SUSPENDIDO_LISTO,
    
    // Estados de suspensión bloqueado
    SUSPENDIDO_BLOQUEADO,
    NO_SUSPENDIDO_BLOQUEADO,
    
    // Estados de reanudación
    REANUDADO,
    NO_REANUDADO
}