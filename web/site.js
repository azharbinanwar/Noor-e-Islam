// Store links: fill these in when each listing is live and every "coming soon" button wakes up.
const STORES = { islam: "", quran: "" };
document.querySelectorAll("[data-store]").forEach(a => {
  const url = STORES[a.dataset.store];
  if (url) { a.href = url; a.classList.remove("soon"); a.textContent = "Get it on Google Play"; }
});
document.querySelectorAll("#year").forEach(e => e.textContent = new Date().getFullYear());
