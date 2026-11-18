// Canvas fallback for browsers without WebGL. Uses the supplied cells scene's
// dimensions and the same two travelling-wave formulas; physical reflections
// remain available in the primary Three.js renderer.
export function startFallback(host, reduced) {
  const canvas = document.createElement('canvas');
  host.replaceChildren(canvas);
  const ctx = canvas.getContext('2d');
  if (!ctx) return;
  let width=0, height=0, frame=0, visible=false, time=0, last=0;
  function paint(now) {
    frame=0;
    if (!visible || document.hidden) return;
    if (!reduced.matches && last) time+=Math.min((now-last)/1000,0.1)*0.7;
    last=now;
    ctx.clearRect(0,0,width,height);
    const step=31, columns=Math.ceil(width/step)+3, rows=Math.ceil(height/step)+3;
    for(let y=0;y<rows;y++) for(let x=0;x<columns;x++) {
      const px=(x-columns/2)*0.23, py=(y-rows/2)*0.23;
      const phase=Math.hypot(px-Math.sin(time*0.2),py-Math.cos(time*0.17))*2.8-time*1.5;
      const energy=0.012+Math.pow(0.5+0.5*Math.sin(phase),10)*0.8
        +Math.pow(0.5+0.5*Math.sin(px*2+py*3+time*1.3),16)*0.27;
      drawTile(ctx,x*step-30,y*step-30,energy);
    }
    if (!reduced.matches) frame=requestAnimationFrame(paint);
  }
  function sync(){cancelAnimationFrame(frame);last=0;if(visible&&!document.hidden)frame=requestAnimationFrame(paint);}
  const resize=new ResizeObserver(()=>{
    width=host.clientWidth;height=host.clientHeight;
    const dpr=Math.min(devicePixelRatio||1,1.5);
    canvas.width=width*dpr;canvas.height=height*dpr;
    ctx.setTransform(dpr,0,0,dpr,0,0);sync();
  });
  resize.observe(host);
  const intersection=new IntersectionObserver(([entry])=>{visible=entry.isIntersecting;sync();});
  intersection.observe(host);
  document.addEventListener('visibilitychange',sync);reduced.addEventListener('change',sync);
  window.addEventListener('pagehide',()=>{cancelAnimationFrame(frame);resize.disconnect();intersection.disconnect();document.removeEventListener('visibilitychange',sync);reduced.removeEventListener('change',sync);},{once:true});
}
function drawTile(ctx,x,y,energy) {
  const outer=ctx.createLinearGradient(x,y,x+26,y+26);
  outer.addColorStop(0,'#32695d');outer.addColorStop(0.4,'#0a2e2c');outer.addColorStop(1,'#041a1b');
  ctx.fillStyle=outer;ctx.beginPath();ctx.roundRect(x,y,26,26,4);ctx.fill();
  ctx.strokeStyle='#679a834d';ctx.lineWidth=0.7;ctx.stroke();
  const brightness=Math.min(1,energy);
  const inner=ctx.createLinearGradient(x,y+5,x+20,y+23);
  inner.addColorStop(0,`rgba(140,225,193,${0.06+brightness*0.75})`);
  inner.addColorStop(1,`rgba(75,164,141,${0.03+brightness*0.65})`);
  ctx.fillStyle=inner;ctx.beginPath();ctx.roundRect(x+5,y+5,16,16,2);ctx.fill();
}
