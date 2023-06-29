import {LitElement, html} from 'lit';
import {customElement, property} from 'lit/decorators.js';
import {repeat} from 'lit/directives/repeat.js';
import {classMap} from 'lit/directives/class-map.js';

import { handleAjaxError } from './utils';

@customElement('tours-view')
export class ToursView extends LitElement {

  @property({type: Boolean})
  busy: boolean;

  @property({type: Array})
  tours: Tour[];

  @property({type: String})
  error: string | null;

  constructor() {
    super();
    this.busy = false;
    this.tours = [];
    this.error = null;
  }

  connectedCallback() {
    super.connectedCallback();
    this.tours = window.tours;

    this.addEventListener('delete-tour', ((e: CustomEvent<number>) => this.onDelete(e)) as EventListener);
    this.addEventListener('update-tour', ((e: CustomEvent<Tour>) => this.onUpdate(e)) as EventListener);
  }

  createRenderRoot() {
    return this;
  }

  onNew(e: Event) {
    e.preventDefault();

    if(this.busy) return;
    this.busy = true;
    this.error = null;

    fetch("/tours", {
      method: "POST",
      headers: {
        'Accept': 'application/json'
      }
    }).then(r => {
      if(r.ok) {
        return r.json();
      } else {
        return handleAjaxError(r, "Failed to create tour");
      }
    }).then((data: Tour) => {
      this.tours.push(data);
      this.busy = false;
    }).catch(err => {
      this.busy = false;
      this.error = err;
    })
  }

  onUpdate(e: CustomEvent<Tour>) {
    e.preventDefault();

    const idx = this.tours.findIndex((t) => t.id == e.detail.id);
    if(idx === -1) {
      console.error("unable to find tour with id ", e.detail.id);
      return;
    }

    if(this.busy) return;
    this.busy = true;

    fetch(`/tours/${e.detail.id}`, {
      method: "PUT",
      body: JSON.stringify(e.detail)
    }).then(r => {
      if(r.ok) {
        return r.json();
      } else {
        return handleAjaxError(r, `Failed to update Tour ${e.detail.id}`);
      }
    }).then((data: Tour) => {
      this.tours.splice(idx, 1, data);
      this.busy = false;
    }).catch(err => {
      this.error = err;
      this.busy = false;
    })
  }

  onDelete(e: CustomEvent) {
    e.preventDefault();

    const idx = this.tours.findIndex((t) => t.id == e.detail);
    if(idx === -1) {
      console.error("unable to find tour with id ", e.detail);
      return;
    }

    const toDelete = this.tours[idx];
    if(!confirm(`Delete Tour ${toDelete.id}`)) {
      return;
    }

    if(this.busy) return;
    this.busy = true;

    fetch(`/tours/${toDelete.id}`, {
      method: "DELETE"
    }).then(r => {
      if(r.ok) {
        this.tours.splice(idx, 1);
        this.busy = false;
      } else {
        this.error = `${r.status}: Failed to delete Tour ${toDelete.id}`;
        this.busy = false;
      }
    })
  }

  override render() {
    const tours = repeat(this.tours, (t) => t.id, (tour, i) => {
      const createdAt = new Date(tour.createdAt).toLocaleString();
      return html`
        <div>
          <tour-view .attrs=${tour}/>
        </div>
      `;
    });

    let error;
    if(this.error !== null) {
      error = html`
        <div class="alert alert-danger">
          ${this.error}
        </div>
      `;
    }

    const busyClass = {"ms-2": true, "min-space": true, "d-none": !this.busy};

    return html`
      <div class="my-2">

        ${error}

        <div class="my-2 d-flex tours-nav justify-content-between">
          <div>
            <div>
              ${this.tours.length} tour(s)
            </div>
          </div>
          <div class="d-flex align-items-center rhs">
            <div>
              <a href="#" class="btn btn-primary" @click=${this.onNew}>+ New</a>
            </div>
            <div class="spinner-container">
              <div class=${classMap(busyClass)}>
                <i class="fa-solid fa-rotate fa-spin"></i>
              </div>
            </div>
          </div>
        </div>
        ${tours}
      </div>
    `;
  }
}