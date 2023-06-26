import { makeAlert } from './utils';

const reposIndex = () => {
  const container = document.querySelector('.repositories .top-area');
  if(container !== null) {
    const button = container.querySelector('.refresh-button')!;
    let busy = false;
    button.addEventListener('click', (e: Event) => {
      if(busy) return;

      busy = true;
      const busyIndicator = container.querySelector('.busy')!;
      busyIndicator.classList.remove('d-none');
      e.preventDefault();
      fetch('/github/loadRepos', {
        method: "PUT"
      }).then(r => {
        busy = false;
        busyIndicator.classList.add('d-none');
        if(r.ok) {
          location.reload();
        } else {
          makeAlert("Failed to fetch repositories");
        }
      });
    });
  }
};

const renderFiles = (id: string, path: string, tbody: Element, dirFiles: GithubFile[]) => {
  dirFiles.forEach((file) =>{
    const tr = document.createElement("tr");
    const cell = document.createElement('td');
    const fileLink = `/repos/${id}${path}/${file.name}`;

    let iconName: string;
    if(file.type === "dir") {
      iconName = 'fa-folder';
    } else {
      iconName = 'fa-file';
    }

    const iconLink = document.createElement('a');
    iconLink.href = fileLink;
    const iTag = document.createElement('i');
    iTag.classList.add('fa-regular');
    iTag.classList.add(iconName);
    iconLink.appendChild(iTag);
    cell.appendChild(iconLink);

    cell.appendChild(document.createTextNode(' '));

    const textLink = document.createElement('a');
    textLink.href = `/repos/${id}${path}/${file.name}`;
    textLink.textContent = file.name;
    cell.appendChild(textLink);

    tr.appendChild(cell);
    tbody.appendChild(tr);
  });
};

const renderFile = (container: Element, file: GithubFile) => {
  if(file.isText) {
    const node = document.createElement("div");
    node.classList.add('wrapper');
    const pre = document.createElement('pre');
    pre.textContent = file.content;
    node.appendChild(pre);
    container.appendChild(node);  
  } else {
    const node = document.createElement('div');
    node.textContent = "This file could not be decoded into UTF-8 text form.";
    container.appendChild(node);  
  }
};

const repoPath = () => {
  const container = document.querySelector('.dir-location');
  if(container !== null) {
    const info = (container as HTMLElement);
    let path: string;
    if(info.dataset.path === undefined) {
      path = "";
    } else {
      path = "/" + info.dataset.path;
    }
    const id: string = info.dataset.repositoryId as string;
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
    }).then((data: GithubPath) => {
      const tbody = container.querySelector('table tbody')!;
      const singleFileContainer = container.querySelector('.single-file')!;

      if(data.isFile) {
        renderFile(singleFileContainer, data.files[0]);
      } else {
        renderFiles(id, path, tbody, data.files);
      }
    }).catch(e => {
      makeAlert(e);
    })
  }
};

export { reposIndex, repoPath };