// / Rileva l'host su cui gira il browser (es: 'localhost', '192.168.1.30', 'eclettici.it')
const hostname = window.location.hostname;

// Se è un ambiente locale (localhost o IP di rete LAN), usa la porta 8082 di Spring Boot
const isLocal = hostname === 'localhost' || hostname === '127.0.0.1' || hostname.startsWith('192.168.') || hostname.startsWith('172.20.');

export const API_BASE_URL = isLocal
  ? `http://${hostname}:8082/api`
  : `https://${hostname}/api`;
