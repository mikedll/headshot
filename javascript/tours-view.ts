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

  override render() {
    const tours = repeat(this.tours, (t) => t.id, (tour, i) => {
      const createdAt = new Date(tour.createdAt).toLocaleString();
      return html`
        <div>
          ${tour.id} - ${tour.name} - ${createdAt}
        </div>
      `;
    });

    let error;
    console.log("tours-view calling render with error", this.error);
    if(this.error !== null) {
      error = html`
        <div class="alert alert-danger">
          ${this.error}
        </div>
      `;
    }

    const busyClass = {"ms-2": true, "d-none": !this.busy};

    return html`
      <div class="my-2">

        ${error}

        <div class="my-2 d-flex align-items-center tours-nav">
          <div>
            ${this.tours.length} tour(s)
          </div>
          <div>
            <a href="#" class="btn btn-primary" @click=${this.onNew}>+ New</a>
          </div>
          <div class=${classMap(busyClass)}>
            <i class="fa-solid fa-rotate fa-spin"></i>
          </div>
        </div>
        ${tours}
      </div>
    `;
  }
}