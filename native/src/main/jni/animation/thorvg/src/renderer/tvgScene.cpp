/*
 * Copyright (c) 2020 - 2024 the ThorVG project. All rights reserved.

 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

#include "tvgScene.h"

/************************************************************************/
/* External Class Implementation                                        */
/************************************************************************/

Scene::Scene() : pImpl(new Impl(this))
{
}


Scene::~Scene()
{
    delete(pImpl);
}


unique_ptr<Scene> Scene::gen() noexcept
{
    return unique_ptr<Scene>(new Scene);
}


TVG_DEPRECATED uint32_t Scene::identifier() noexcept
{
    return (uint32_t) Type::Scene;
}


Type Scene::type() const noexcept
{
    return Type::Scene;
}


Result Scene::push(unique_ptr<Paint> paint) noexcept
{
    auto p = paint.release();
    if (!p) return Result::MemoryCorruption;
    PP(p)->ref();
    pImpl->paints.push_back(p);

    return Result::Success;
}


Result Scene::clear(bool free) noexcept
{
    pImpl->clear(free);

    return Result::Success;
}


list<Paint*>& Scene::paints() noexcept
{
    return pImpl->paints;
}