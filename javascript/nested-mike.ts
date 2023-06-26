import {LitElement, html} from 'lit';
import {customElement, property} from 'lit/decorators.js';

@customElement('nested-mike')
export default class NestedMike extends LitElement {

  @property({type: Object})
  attrs?: NestedMikeAttrs;

  createRenderRoot() {
    return this;
  }

  override render() {
    return html`
      <div class="mt-2">
        Nested Mike
        <br>
        Name: ${this.attrs?.name}
        Age: ${this.attrs?.age}
      </div>
    `;
  }
}