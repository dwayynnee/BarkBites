// Bark Bites Web App - Client-side JavaScript
console.log('Bark Bites web app loaded!');

// Example: Fetch health status from server
async function checkServerStatus() {
  try {
    const response = await fetch('/api/health');
    const data = await response.json();
    console.log('Server status:', data);
  } catch (error) {
    console.error('Error checking server status:', error);
  }
}

// Run on page load
document.addEventListener('DOMContentLoaded', () => {
  checkServerStatus();
});
