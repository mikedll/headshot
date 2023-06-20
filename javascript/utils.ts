
const makeAlert = (message: string) => {
  const container = document.querySelector('.alerts-container');
  if(container !== null) {
    container.replaceChildren();
    const alertBox = document.createElement('div');
    alertBox.classList.add("alert");
    alertBox.classList.add("alert-danger");
    alertBox.textContent = message;
    container.appendChild(alertBox);
  } else {
    console.error("Unable to make alert for message", message);
  }
};

export { makeAlert };