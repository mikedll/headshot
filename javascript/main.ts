
const repos = () => {

  const container = document.querySelector('.repositories');
  if(container !== null) {
    console.log("repos handler", "binding because we found our container");
    const button = container.querySelector('.refresh-button')!;
    button.addEventListener('click', (e: Event) => {
      e.preventDefault();
      fetch('/repos/load', {
        method: "PUT"
      }).then(r => {
        if(r.ok) {
          location.reload();
        } else {
          console.error("Failed to fetch repositories");
        }
      });
    });
  }
};

document.addEventListener("DOMContentLoaded", () => {
  repos();
});

