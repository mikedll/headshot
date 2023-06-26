import {LitElement, html} from 'lit';
import {customElement, property} from 'lit/decorators.js';

@customElement('basic-component')
export default class BasicComponent extends LitElement {

  createRenderRoot() {
    return this;
  }

  override render() {
    const attrs: NestedMikeAttrs = {name: "Sally", age: 30};

    return html`
      <div class="mt-2">
        Hello from Lit
        <nested-mike .attrs=${attrs}/>
      </div>
    `;
  }
}