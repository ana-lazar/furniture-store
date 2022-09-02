import Koa from "koa";
import Http from "http";
import WebSocket from "ws";
import Router from "koa-router";
import cors from "koa-cors";
import bodyparser from "koa-bodyparser";

const app = new Koa();
const server = Http.createServer(app.callback());
const wss = new WebSocket.Server({ server });

app.use(bodyparser());
app.use(cors());
app.use(async (ctx, next) => {
  const start = new Date();
  await next();
  const ms = new Date() - start;
  console.log(`${ctx.method} ${ctx.url} ${ctx.response.status} - ${ms}ms`);
});
app.use(async (ctx, next) => {
  await new Promise((resolve) => setTimeout(resolve, 2000));
  await next();
});
app.use(async (ctx, next) => {
  try {
    await next();
  } catch (err) {
    ctx.response.body = {
      issue: [{ error: err.message || "Unexpected error" }],
    };
    ctx.response.status = 500;
  }
});

class Product {
  constructor({
    _id,
    name,
    description,
    company,
    quantity,
    category,
    isle,
    version,
    date,
    local,
  }) {
    this._id = _id;
    this.name = name;
    this.description = description;
    this.company = company;
    this.quantity = quantity;
    this.category = category;
    this.isle = isle;
    this.version = version;
    this.date = date;
    this.local = local;
  }
}

const products = [];
for (let i = 0; i < 3; i++) {
  products.push(
    new Product({
      _id: `${i}`,
      name: `product ${i}`,
      description: `description ${i}`,
      company: `company ${i}`,
      quantity: 100 + i,
      category: `category ${i}`,
      isle: `isle ${i}`,
      version: 1,
      date: new Date(Date.now() + i),
      local: false,
    })
  );
}
var lastId = products[products.length - 1]._id;
var lastUpdated = products[products.length - 1].date;

const broadcast = (data) =>
  wss.clients.forEach((client) => {
    if (client.readyState === WebSocket.OPEN) {
      client.send(JSON.stringify(data));
    }
  });

const router = new Router();

router.get("/products", (ctx) => {
  const ifModifiedSince = ctx.request.get("If-Modified-Since");
  if (
    ifModifiedSince &&
    new Date(ifModifiedSince).getTime() >=
      lastUpdated.getTime() - lastUpdated.getMilliseconds()
  ) {
    // NOT MODIFIED
    ctx.response.status = 304;
    return;
  }
  const text = ctx.request.query.text;
  ctx.response.set("Last-Modified", lastUpdated.toUTCString());
  ctx.response.body = products;
  ctx.response.status = 200;
});

router.get("/product/:id", async (ctx) => {
  const productId = ctx.request.params.id;
  const product = products.find((product) => productId === product.id);
  if (product) {
    // OK
    ctx.response.body = product;
    ctx.response.status = 200;
  } else {
    // NOT FOUND
    ctx.response.body = {
      issue: [{ warning: `Product with_id ${productId} not found` }],
    };
    ctx.response.status = 404;
  }
});

const createProduct = async (ctx) => {
  const product = ctx.request.body;
  if (
    !product.name ||
    !product.description ||
    !product.quantity ||
    !product.category ||
    !product.company ||
    !product.isle
  ) {
    // BAD REQUEST
    ctx.response.body = { issue: [{ error: "Field is missing" }] };
    ctx.response.status = 400;
    return;
  }
  product._id = `${parseInt(lastId) + 1}`;
  lastId = product._id;
  product.date = new Date();
  product.version = 1;
  product.local = false;
  products.push(product);
  // CREATED
  ctx.response.body = product;
  ctx.response.status = 200;
  broadcast({ event: "created", payload: product });
};

router.post("/product", async (ctx) => {
  await createProduct(ctx);
});

router.put("/product/:id", async (ctx) => {
  const id = ctx.params.id;
  const product = ctx.request.body;
  product.date = new Date();
  const productId = product._id;
  if (productId && id !== product._id) {
    // BAD REQUEST
    ctx.response.body = {
      issue: [{ error: `Param id and body id should be the same` }],
    };
    ctx.response.status = 400;
    return;
  }
  if (!productId) {
    await createProduct(ctx);
    return;
  }
  const index = products.findIndex((product) => product._id === id);
  if (index === -1) {
    // BAD REQUEST
    ctx.response.body = {
      issue: [{ error: `Product with id ${id} not found` }],
    };
    ctx.response.status = 400;
    return;
  }
  const productVersion = product.version;
  if (productVersion < products[index].version) {
    // CONFLICT
    ctx.response.body = { issue: [{ error: `Version conflict` }] };
    ctx.response.status = 409;
    return;
  }
  product.version++;
  product.local = false;
  products[index] = product;
  lastUpdated = new Date();
  // OK
  ctx.response.body = product;
  ctx.response.status = 200;
  broadcast({ event: "updated", payload: product });
});

router.del("/product/:id", (ctx) => {
  const id = ctx.params.id;
  const index = products.findIndex((product) => id === product._id);
  if (index !== -1) {
    const product = products[index];
    products.splice(index, 1);
    lastUpdated = new Date();
    broadcast({ event: "deleted", payload: product });
  }
  // NO CONTENT
  ctx.response.body = {};
  ctx.response.status = 204;
});

app.use(router.routes());
app.use(router.allowedMethods());

server.listen(3000);
