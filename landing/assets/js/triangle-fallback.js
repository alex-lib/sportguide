// Software renderer of the supplied beam geometry for browsers without WebGL.
import * as T from './three.module.js';
import {buildBeam, LEG, VIEW} from './triangle-core.js';

function makePatterns(ctx) {
  return [['#275e53','#e4efe5'],['#ffad6e','#103b35'],['#73bda1','#103b35']].map(([fill,ink],i)=>{
    const strip=document.createElement('canvas');strip.width=270;strip.height=80;
    const c=strip.getContext('2d');c.fillStyle=fill;c.fillRect(0,0,270,80);
    c.fillStyle=ink;c.font=`${i===1?800:600} 36px Montserrat`;c.textBaseline='middle';
    c.fillText('sportguide',12,40);return ctx.createPattern(strip,'repeat');
  });
}
function prepare(width,height) {
  const beamWidth=LEG*(0.14+0.02*4.2),radius=beamWidth/2+beamWidth*0.08*3.6;
  const {geometry,cuts,length}=buildBeam(beamWidth,radius);
  const target=new T.Vector3(LEG/3,2*LEG/3,0);
  const ppu=Math.min(width*0.57,height*0.41)*1.5/(LEG*Math.sqrt(2/3));
  const camera=new T.OrthographicCamera(-width/2/ppu,width/2/ppu,height/2/ppu,-height/2/ppu,0.1,40);
  camera.position.copy(target).addScaledVector(VIEW,10*LEG);camera.lookAt(target);camera.updateMatrixWorld();
  const p=geometry.attributes.position,n=geometry.attributes.normal,u=geometry.attributes.aU,v=geometry.attributes.aV;
  const triangles=[];const indices=geometry.index.array;
  for(let i=0;i<indices.length;i+=3){
    const ids=[indices[i],indices[i+1],indices[i+2]];
    const normal=new T.Vector3(...ids.reduce((a,id)=>a.map((x,j)=>x+n.array[id*3+j]/3),[0,0,0]));
    if(normal.dot(VIEW)<=0.001)continue;
    const band=normal.z>=normal.x&&normal.z>=normal.y?0:normal.y>=normal.x?1:2;
    const sign=band===0?1:-1;
    const points=ids.map(id=>{const world=new T.Vector3().fromBufferAttribute(p,id);const depth=world.dot(VIEW);const pos=world.project(camera);return {x:(pos.x+1)*width/2,y:(1-pos.y)*height/2,depth,u:sign*(u.getX(id)-(u.getX(id)>=cuts[band]?length:0))/beamWidth*80,v:(0.5-sign*v.getX(id)/beamWidth)*80};});
    triangles.push({points,band,depth:points.reduce((s,p)=>s+p.depth,0)/3});
  }
  geometry.dispose();return triangles.sort((a,b)=>a.depth-b.depth);
}
function paintTriangle(ctx,triangle,pattern,offset) {
  const [p,q,r]=triangle.points;
  const u1=q.u-p.u,v1=q.v-p.v,u2=r.u-p.u,v2=r.v-p.v,det=u1*v2-u2*v1;
  if(Math.abs(det)<0.0001)return;
  const a=((q.x-p.x)*v2-(r.x-p.x)*v1)/det,b=((q.y-p.y)*v2-(r.y-p.y)*v1)/det;
  const c=((r.x-p.x)*u1-(q.x-p.x)*u2)/det,d=((r.y-p.y)*u1-(q.y-p.y)*u2)/det;
  const phase=(triangle.band===0?1:-1)*offset;
  const cx=(p.x+q.x+r.x)/3,cy=(p.y+q.y+r.y)/3;
  const expanded=[p,q,r].map(pt=>{const dx=pt.x-cx,dy=pt.y-cy,len=Math.hypot(dx,dy)||1;return {x:pt.x+dx/len*0.45,y:pt.y+dy/len*0.45};});
  ctx.save();ctx.beginPath();ctx.moveTo(expanded[0].x,expanded[0].y);ctx.lineTo(expanded[1].x,expanded[1].y);ctx.lineTo(expanded[2].x,expanded[2].y);ctx.closePath();ctx.clip();
  ctx.transform(a,b,c,d,p.x-a*(p.u-phase)-c*p.v,p.y-b*(p.u-phase)-d*p.v);
  ctx.fillStyle=pattern;
  const lo=Math.min(p.u,q.u,r.u)-phase-2,hi=Math.max(p.u,q.u,r.u)-phase+2;
  ctx.fillRect(lo,-2,hi-lo,84);ctx.restore();
}
export function startTriangleFallback(host) {
  const canvas=document.createElement('canvas');host.replaceChildren(canvas);
  const ctx=canvas.getContext('2d');if(!ctx)return;
  const reduced=matchMedia('(prefers-reduced-motion: reduce)');
  let triangles=[],patterns=[],width=0,height=0,frame=0,visible=false,offset=0,last=0;
  function draw(now){frame=0;if(!visible||document.hidden)return;
    if(!reduced.matches&&last)offset=(offset+Math.min(now-last,100)*0.025)%270;
    last=now;ctx.clearRect(0,0,width,height);
    for(const triangle of triangles)paintTriangle(ctx,triangle,patterns[triangle.band],offset);
    if(!reduced.matches)frame=requestAnimationFrame(draw);
  }
  function sync(){cancelAnimationFrame(frame);last=0;if(visible&&!document.hidden)frame=requestAnimationFrame(draw);}
  function resize(){width=host.clientWidth;height=host.clientHeight;const dpr=Math.min(devicePixelRatio||1,1.5);
    canvas.width=width*dpr;canvas.height=height*dpr;ctx.setTransform(dpr,0,0,dpr,0,0);
    triangles=prepare(width,height);patterns=makePatterns(ctx);sync();}
  const ro=new ResizeObserver(resize);ro.observe(host);
  const io=new IntersectionObserver(([e])=>{visible=e.isIntersecting;sync();});io.observe(host);
  document.addEventListener('visibilitychange',sync);reduced.addEventListener('change',sync);
  document.fonts.ready.then(resize);
  window.addEventListener('pagehide',()=>{cancelAnimationFrame(frame);ro.disconnect();io.disconnect();document.removeEventListener('visibilitychange',sync);reduced.removeEventListener('change',sync);},{once:true});
}
