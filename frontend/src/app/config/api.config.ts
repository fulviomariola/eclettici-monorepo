// Rileva automaticamente l'IP/Hostname su cui sta girando Angular (es. localhost oppure 192.168.1.X)
const hostname = window.location.hostname;

export const API_BASE_URL = `http://${hostname}:8082/api`;
