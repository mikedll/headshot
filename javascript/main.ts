
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

const repos = () => {
  const container = document.querySelector('.repositories');
  if(container !== null) {
    const button = container.querySelector('.refresh-button')!;
    button.addEventListener('click', (e: Event) => {
      e.preventDefault();
      fetch('/repos/load', {
        method: "PUT"
      }).then(r => {
        if(r.ok) {
          location.reload();
        } else {
          makeAlert("Failed to fetch repositories");
        }
      });
    });
  }
};

const dirLocation = () => {
  const container = document.querySelector('.dir-location');
  if(container !== null) {
    const info = (container as HTMLElement);
    let path: string;
    if(info.dataset.path === undefined) {
      path = "";
    } else {
      path = "/" + info.dataset.path;
    }
    const id = info.dataset.repositoryId;
    fetch(`/github/readDir/${id}${path}`, {
      headers: {
        "Accept": "application/json"
      }
    })
    .then(r => {
      if(r.ok) {
        return r.json();
      } else {
        return r.json()
        .then(data => {
          if(data === undefined) {
            throw `${r.status} Internal Server Error: Failed to get directory listing`;
          }

          if('error' in data) {
            throw `Failed to get directory listing: ${data.error}`;
          } else {
            throw "Failed to get directory listing (and failed to parse JSON response)";
          }
        })
        .catch(e => {
          throw "Failed to get directory listing (and failed to parse JSON response)";
        });
      }
    }).then((data: DirFile[]) => {
      const tbody = container.querySelector('table tbody')!;
      data.forEach((file) =>{
        const tr = document.createElement("tr");
        const cell = document.createElement('td');
        const fileLink = `/repos/${id}${path}/${file.name}`;
        if(file.type === "dir") {
          const iconLink = document.createElement('a');
          iconLink.href = fileLink;
          const iTag = document.createElement('i');
          iTag.classList.add('fa-regular');
          iTag.classList.add('fa-folder');
          iconLink.appendChild(iTag);
          cell.appendChild(iconLink);

          cell.appendChild(document.createTextNode(' '));

          const textLink = document.createElement('a');
          textLink.href = `/repos/${id}${path}/${file.name}`;
          textLink.textContent = file.name;
          cell.appendChild(textLink);
        } else {
          const iTag = document.createElement('i');
          iTag.classList.add('fa-regular');
          iTag.classList.add('fa-file');
          cell.appendChild(iTag);
          cell.appendChild(document.createTextNode(' '));
          cell.appendChild(document.createTextNode(file.name));
        }
        tr.appendChild(cell);
        tbody.appendChild(tr);
      });
    }).catch(e => {
      makeAlert(e);
    })
  }
};

document.addEventListener("DOMContentLoaded", () => {
  repos();
  dirLocation();
});

