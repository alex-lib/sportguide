import * as T from './three.module.js';
import { cells, environment } from './cells.js';
import { startFallback } from './cells-fallback.js';

const host = document.querySelector('.cell-scene');
const reduced = matchMedia('(prefers-reduced-motion: reduce)');

function createScene() {
  const canvas = document.createElement('canvas');
  const context = canvas.getContext('webgl2', { antialias: true, alpha: true });
  if (!context) throw new Error('WebGL unavailable');
  const renderer = new T.WebGLRenderer({ canvas, context, antialias: true, alpha: true });
  renderer.outputColorSpace = T.SRGBColorSpace;
  renderer.toneMapping = T.ACESFilmicToneMapping;
  renderer.toneMappingExposure = 0.85;
  host.append(renderer.domElement);
  const scene = new T.Scene();
  const camera = new T.PerspectiveCamera(40, 1, 0.05, 150);
  const root = new T.Group();
  scene.add(root, new T.HemisphereLight(0xd5fff1, 0x042729, 0.65));
  const key = new T.DirectionalLight(0xc5fff0, 3);
  key.position.set(-3, 5, 6);
  scene.add(key);
  const env = environment(renderer);
  scene.environment = env.texture;
  const time = { value: 0 };
  const dark = new T.MeshPhysicalMaterial({color:'#164e46',metalness:0.88,roughness:0.29,clearcoat:1,envMapIntensity:0.8});
  cells({root, time, dark, aim(x,y,z){camera.position.set(x,y,z);camera.lookAt(0,0,0);}});
  return {renderer, scene, camera, env, time};
}

function runScene(state) {
  const {renderer,scene,camera,time} = state;
  let frame = 0;
  let visible = false;
  let last = 0;
  function draw(now) {
    frame = 0;
    if (!visible || document.hidden) return;
    if (!reduced.matches) time.value += last ? Math.min((now-last)/1000,0.1) * 0.7 : 0;
    last = now;
    renderer.render(scene,camera);
    if (!reduced.matches) frame = requestAnimationFrame(draw);
  }
  function sync() {
    cancelAnimationFrame(frame);
    last = 0;
    if (visible && !document.hidden) frame = requestAnimationFrame(draw);
  }
  function resize() {
    const {width,height} = host.getBoundingClientRect();
    renderer.setPixelRatio(Math.min(devicePixelRatio || 1, 1.5, Math.sqrt(2200000/(width*height))));
    renderer.setSize(width,height,false);
    camera.aspect = width/height;
    camera.updateProjectionMatrix();
    sync();
  }
  const resizeObserver = new ResizeObserver(resize);
  resizeObserver.observe(host);
  const observer = new IntersectionObserver(([entry]) => {visible=entry.isIntersecting;sync();});
  observer.observe(host);
  document.addEventListener('visibilitychange',sync);
  reduced.addEventListener('change',sync);
  window.addEventListener('pagehide',()=>{
    cancelAnimationFrame(frame);observer.disconnect();resizeObserver.disconnect();
    document.removeEventListener('visibilitychange',sync);reduced.removeEventListener('change',sync);
    scene.traverse(o=>{o.geometry?.dispose();o.material?.dispose();});
    state.env.dispose();renderer.dispose();
  },{once:true});
  resize();
}
try { runScene(createScene()); } catch (error) {
  startFallback(host, reduced);
  host.dataset.fallback='true';
}
