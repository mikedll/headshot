import {LitElement, html, TemplateResult} from 'lit';
import {customElement, property} from 'lit/decorators.js';
import {repeat} from 'lit/directives/repeat.js';
import {classMap} from 'lit/directives/class-map.js';

import { handleAjaxError } from './utils';

@customElement('tour-view')
export class TourView extends LitElement {

  @property({type: Boolean})
  busy: boolean;

  @property({type: Boolean})
  editing: boolean;

  @property({type: Array})
  attrs: Tour | null;

  @property({type: String})
  error: string | null;

  constructor() {
    super();
    this.busy = false;
    this.editing = false;
    this.attrs = null;
    this.error = null;
  }

  createRenderRoot() {
    return this;
  }

  onDelete(e: Event, id: number) {
    e.preventDefault();
    this.dispatchEvent(new CustomEvent<number>('delete-tour', {detail: id, bubbles:true}));
  }

  enterEditing(e: Event) {
    e.preventDefault();
    this.editing = true;
  }

  setName(e: Event) {
    this.attrs!.name = (e.target! as HTMLInputElement).value;
  }

  keyUp(e: KeyboardEvent) {
    if(e.key === 'Enter') {
      this.setName(e);
      this.dispatchEvent(new CustomEvent<Tour>('update-tour', {detail: this.attrs!, bubbles: true}));
      this.editing = false;
    } else if(e.key === 'Escape') {
      this.editing = false;
    }
  }

  override render() {
    const attrs = this.attrs!;

    let nameArea: TemplateResult;
    if(this.editing === true) {
      nameArea = html`<input type="text" name="name" .value=${this.attrs!.name} @blur=${this.setName} @keyup=${this.keyUp}></input>`
    } else {
      nameArea = html`${attrs.name} <a href="#" @click=${this.enterEditing}>[!]</a>`;
    }

    const createdAt = new Date(attrs.createdAt).toLocaleString();

    return html`
      <div class="my-2">
        ${attrs.id} - ${nameArea} - ${createdAt} <a href="#" @click=${(e: Event) => this.onDelete(e, attrs.id)}>[x]</a>
      </div>
    `;
  }
}