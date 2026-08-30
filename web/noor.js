// Shared behavior for inner pages: lattice background, reveals, store buttons.
if (new URLSearchParams(location.search).has("app")) document.body.classList.add("embedded");
document.querySelectorAll("#year").forEach(e => e.textContent = new Date().getFullYear());

// Store links: fill these in when each listing is live and every "coming soon" button wakes up.
const STORES = { islam: "", quran: "" };
document.querySelectorAll("[data-store]").forEach(a => {
  const url = STORES[a.dataset.store];
  if (url) { a.href = url; a.classList.remove("soon"); a.textContent = "Get it on Google Play"; }
});

const io = new IntersectionObserver(es => es.forEach(e => {
  if (e.isIntersecting) { e.target.classList.add("in"); io.unobserve(e.target); }
}), { threshold: .12 });
document.querySelectorAll(".feature").forEach(el => io.observe(el));

// Noor lattice: an eight point star screen, lit softly where the light wanders.
const cv = document.getElementById("stars");
if (cv) {
  const cx = cv.getContext("2d");
  const reduced = matchMedia("(prefers-reduced-motion: reduce)").matches;
  const CELL = 96;
  const size = () => { cv.width = innerWidth; cv.height = innerHeight; };
  const starPath = (x, y, R) => {
    const r = R * .42;
    cx.beginPath();
    for (let i = 0; i < 16; i++) {
      const a = i * Math.PI / 8 - Math.PI / 2, rad = i % 2 ? r : R;
      const px = x + rad * Math.cos(a), py = y + rad * Math.sin(a);
      i ? cx.lineTo(px, py) : cx.moveTo(px, py);
    }
    cx.closePath();
  };
  const frame = t => {
    cx.clearRect(0, 0, cv.width, cv.height);
    const L = [
      { x: cv.width * (.5 + .42 * Math.sin(t * .00009)), y: cv.height * (.5 + .4 * Math.cos(t * .00007)) },
      { x: cv.width * (.5 + .42 * Math.cos(t * .00006)), y: cv.height * (.5 + .4 * Math.sin(t * .00011)) },
    ];
    const off = (scrollY * .15) % CELL;
    cx.lineWidth = 1;
    for (let gy = -1; gy * CELL - off < cv.height + CELL; gy++) {
      for (let gx = -1; gx * CELL < cv.width + CELL; gx++) {
        const x = gx * CELL + (gy % 2 ? CELL / 2 : 0), y = gy * CELL - off;
        let lit = 0;
        for (const l of L) {
          const d = Math.hypot(x - l.x, y - l.y);
          lit = Math.max(lit, Math.max(0, 1 - d / (cv.width * .3)));
        }
        const breathe = .5 + .5 * Math.sin(t * .0004 + gx * 1.7 + gy * 2.3);
        const a = .028 + lit * lit * (.14 + .05 * breathe);
        cx.strokeStyle = `rgba(127,200,169,${a})`;
        starPath(x, y, CELL * .46);
        cx.stroke();
      }
    }
    if (!reduced) requestAnimationFrame(frame);
  };
  size(); requestAnimationFrame(frame);
  addEventListener("resize", () => { size(); if (reduced) requestAnimationFrame(frame); });
}
