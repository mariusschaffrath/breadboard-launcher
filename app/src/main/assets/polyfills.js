// BreadBoard Launcher - Apple HomeView Polyfills for Chromium 59 / Android 5.1
console.log('[HomeView] Initializing Chromium 59 compatibility shims...');

// 1. Home Assistant Authentication Token Injection
try {
  var haTokens = {
    access_token: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJkNzk0MWVhOWQ5OWU0YzhjOTc0YjU5NjE4N2Q1YjI0OSIsImlhdCI6MTc4NzkzMDIzNSwiZXhwIjoyMTAzMjkwMjM1fQ.Hx4GbbBbAPN9ToA2opU7wpyIZ5GV1bmgDNHNLswLG9M',
    token_type: 'Bearer',
    expires: 4102444800000,
    expires_in: 315360000,
    hassUrl: 'http://192.168.178.131:8123',
    clientId: 'http://192.168.178.131:8123/'
  };
  window.localStorage.setItem('hassTokens', JSON.stringify(haTokens));
} catch(e) {
  console.error('[HomeView] Failed to save hassTokens:', e);
}

// 2. Global Environment Shims
if (typeof globalThis === 'undefined') { window.globalThis = window; }
if (typeof queueMicrotask === 'undefined') {
  window.queueMicrotask = function(cb) { Promise.resolve().then(cb); };
}

// 3. Custom Elements ES5 Adapter
// Fixes "Failed to construct HTMLElement: this instance is already constructed"
(function() {
  if (!window.customElements) return;
  var OrigHTMLElement = window.HTMLElement;
  window.HTMLElement = function HTMLElement() {
    return Reflect.construct(OrigHTMLElement, [], this.constructor);
  };
  HTMLElement.prototype = OrigHTMLElement.prototype;
  HTMLElement.prototype.constructor = HTMLElement;
  if (Object.setPrototypeOf) {
    Object.setPrototypeOf(HTMLElement, OrigHTMLElement);
  }
})();

// 4. HTMLSlotElement & Element assignedElements
if (window.HTMLSlotElement && !HTMLSlotElement.prototype.assignedElements) {
  HTMLSlotElement.prototype.assignedElements = function(opts) {
    return this.assignedNodes(opts).filter(function(n) { return n.nodeType === 1; });
  };
}
if (window.Element && !Element.prototype.assignedElements) {
  Element.prototype.assignedElements = function(opts) {
    return typeof this.assignedNodes === 'function'
      ? this.assignedNodes(opts).filter(function(n) { return n.nodeType === 1; })
      : [];
  };
}

// 5. Safe scrollTo & scrollBy for both Window and Element
(function() {
  function safeScroll(orig) {
    return function(x, y) {
      try {
        if (typeof x === 'object' && x !== null) {
          orig.call(this, (x.left || 0), (x.top || 0));
        } else if (arguments.length >= 2) {
          orig.call(this, Number(x) || 0, Number(y) || 0);
        } else if (arguments.length === 1) {
          orig.call(this, Number(x) || 0, 0);
        } else {
          orig.call(this, 0, 0);
        }
      } catch(e) {}
    };
  }
  if (window.scrollTo) { window.scrollTo = safeScroll(window.scrollTo); }
  if (window.scrollBy) { window.scrollBy = safeScroll(window.scrollBy); }
  if (window.Element) {
    if (Element.prototype.scrollTo) Element.prototype.scrollTo = safeScroll(Element.prototype.scrollTo);
    if (Element.prototype.scrollBy) Element.prototype.scrollBy = safeScroll(Element.prototype.scrollBy);
  }
})();

// 6. ResizeObserver Polyfill
if (!window.ResizeObserver) {
  window.ResizeObserver = function(callback) {
    this.callback = callback;
    this.targets = [];
    var self = this;
    this.check = function() {
      var entries = [];
      for (var i = 0; i < self.targets.length; i++) {
        var t = self.targets[i];
        var rect = t.getBoundingClientRect();
        if (t.__lastW !== rect.width || t.__lastH !== rect.height) {
          t.__lastW = rect.width;
          t.__lastH = rect.height;
          entries.push({
            target: t,
            contentRect: rect,
            borderBoxSize: [{ inlineSize: rect.width, blockSize: rect.height }],
            contentBoxSize: [{ inlineSize: rect.width, blockSize: rect.height }]
          });
        }
      }
      if (entries.length > 0) {
        try { self.callback(entries, self); } catch(e) { console.error(e); }
      }
    };
  };
  window.ResizeObserver.prototype.observe = function(target) {
    if (this.targets.indexOf(target) === -1) {
      this.targets.push(target);
      var rect = target.getBoundingClientRect();
      target.__lastW = rect.width;
      target.__lastH = rect.height;
      if (!this.timer) {
        var self = this;
        this.timer = setInterval(function() { self.check(); }, 200);
      }
      var self = this;
      window.requestAnimationFrame(function() { self.check(); });
    }
  };
  window.ResizeObserver.prototype.unobserve = function(target) {
    var idx = this.targets.indexOf(target);
    if (idx !== -1) { this.targets.splice(idx, 1); }
    if (this.targets.length === 0 && this.timer) {
      clearInterval(this.timer);
      this.timer = null;
    }
  };
  window.ResizeObserver.prototype.disconnect = function() {
    this.targets = [];
    if (this.timer) { clearInterval(this.timer); this.timer = null; }
  };
}

// 7. Promise.allSettled
if (!Promise.allSettled) {
  Promise.allSettled = function(promises) {
    return Promise.all(promises.map(function(p) {
      return Promise.resolve(p).then(
        function(value) { return { status: 'fulfilled', value: value }; },
        function(reason) { return { status: 'rejected', reason: reason }; }
      );
    }));
  };
}

// 8. String.prototype.replaceAll
if (!String.prototype.replaceAll) {
  String.prototype.replaceAll = function(str, newStr) {
    if (Object.prototype.toString.call(str).toLowerCase() === '[object regexp]') {
      return this.replace(str, newStr);
    }
    return this.replace(new RegExp(str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&'), 'g'), newStr);
  };
}

// 9. Array & String .at
if (!Array.prototype.at) {
  Array.prototype.at = function(n) {
    n = Math.trunc(n) || 0;
    if (n < 0) n += this.length;
    if (n < 0 || n >= this.length) return undefined;
    return this[n];
  };
}
if (!String.prototype.at) {
  String.prototype.at = function(n) {
    n = Math.trunc(n) || 0;
    if (n < 0) n += this.length;
    if (n < 0 || n >= this.length) return undefined;
    return this[n];
  };
}

// 10. Array flat & flatMap
if (!Array.prototype.flat) {
  Array.prototype.flat = function(depth) {
    var d = depth === undefined ? 1 : Math.floor(depth);
    if (d < 1) return Array.prototype.slice.call(this);
    return (function flatten(arr, d) {
      return d > 0 ? arr.reduce(function(acc, val) {
        return acc.concat(Array.isArray(val) ? flatten(val, d - 1) : val);
      }, []) : arr.slice();
    })(this, d);
  };
}
if (!Array.prototype.flatMap) {
  Array.prototype.flatMap = function(fn, thisArg) {
    return this.map(fn, thisArg).flat(1);
  };
}

// 11. Object.hasOwn & Object.fromEntries & structuredClone
if (!Object.hasOwn) {
  Object.hasOwn = function(obj, prop) {
    return Object.prototype.hasOwnProperty.call(obj, prop);
  };
}
if (!Object.fromEntries) {
  Object.fromEntries = function(entries) {
    var obj = {};
    for (var pair of entries) { obj[pair[0]] = pair[1]; }
    return obj;
  };
}
if (!window.structuredClone) {
  window.structuredClone = function(o) { return JSON.parse(JSON.stringify(o)); };
}

// 12. Active Kiosk Enforcer (cleans up any stray UI elements from Shadow DOM)
(function() {
  function enforceKiosk() {
    try {
      var ha = document.querySelector('home-assistant');
      if (!ha || !ha.shadowRoot) return;
      var main = ha.shadowRoot.querySelector('home-assistant-main');
      if (!main || !main.shadowRoot) return;
      var sidebar = main.shadowRoot.querySelector('ha-sidebar');
      if (sidebar && sidebar.style.display !== 'none') sidebar.style.display = 'none';
      var drawer = main.shadowRoot.querySelector('ha-drawer');
      if (drawer) {
        var header = drawer.querySelector('app-header, ha-top-app-bar');
        if (header && header.style.display !== 'none') header.style.display = 'none';
      }
    } catch(e) {}
  }
  setInterval(enforceKiosk, 400);
})();

console.log('[HomeView] Chromium 59 compatibility shims ready.');
