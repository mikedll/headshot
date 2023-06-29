
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

/*
  Takes a non-200 response and given error message, extracts any json error
  in the response, and throws a string tailored with what it found.
  
  Mean to be called like thils:

  fetch('some/where').then(r => {
    if(r.ok) {
      // do something. probably parse the json.
    } else {
      return handleAjaxError(r, "Error doing fetch X")
    }
  })
*/
const handleAjaxError = (r: Response, msg: string) => {
  return r.json()
  .then(data => {
    if(data === undefined) {
      throw `${r.status} Internal Server Error: ${msg}`;
    }
  
    if('error' in data) {
      throw `${msg}: ${data.error}`;
    } else {
      throw `${msg} (and failed to parse JSON response)`;
    }
  })
  .catch(err => {
    // json parse error I guess. Propagate to outer fetch catch.
    throw `${msg} (and failed to parse JSON response)`;
  });  
}

export { makeAlert, handleAjaxError };